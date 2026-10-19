package org.twins.core.unit.service.twin;

import org.cambium.common.exception.ServiceException;
import org.cambium.common.kit.Kit;
import org.cambium.common.util.UuidUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.quality.Strictness;
import org.springframework.data.jpa.domain.Specification;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.dao.datalist.DataListEntity;
import org.twins.core.dao.datalist.DataListOptionEntity;
import org.twins.core.dao.twin.TwinRepository;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.dao.twinclass.TwinClassRepository;
import org.twins.core.domain.EntityRelinkOperation;
import org.twins.core.enums.EntityRelinkOperationStrategy;
import org.twins.core.service.datalist.DataListService;
import org.twins.core.service.twin.TwinFlavorService;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

// Lenient: the service fans out to several repo methods per call and not every stub is exercised
// by every scenario; we assert behavior with verify()/assert* rather than relying on strict stubs.
@org.mockito.junit.jupiter.MockitoSettings(strictness = Strictness.LENIENT)
class TwinFlavorServiceTest extends BaseUnitTest {

    @Mock private TwinRepository twinRepository;
    @Mock private TwinClassRepository twinClassRepository;
    @Mock private DataListService dataListService;

    @InjectMocks private TwinFlavorService twinFlavorService;

    private UUID classId;
    private TwinClassEntity twinClass;

    @BeforeEach
    void setUp() {
        classId = UUID.randomUUID();
        twinClass = new TwinClassEntity().setId(classId);
        // by default: no descendants, no existing flavors, no flavor-less twins (count defaults to 0)
        when(twinClassRepository.findAll(any(Specification.class))).thenReturn(List.of());
        when(twinRepository.findDistinctFlavorDataListOptionIdByTwinClassIdIn(any())).thenReturn(Set.of());
    }

    private DataListEntity dataList(UUID id, UUID... optionIds) {
        DataListEntity dl = new DataListEntity().setId(id);
        List<DataListOptionEntity> options = new ArrayList<>();
        for (UUID oid : optionIds)
            options.add(new DataListOptionEntity().setId(oid).setDataListId(id));
        dl.setOptions(new Kit<>(options, DataListOptionEntity::getId));
        return dl;
    }

    private EntityRelinkOperation op(UUID newId, EntityRelinkOperationStrategy strategy, Map<UUID, UUID> replaceMap) {
        return new EntityRelinkOperation().setNewId(newId).setStrategy(strategy).setReplaceMap(replaceMap);
    }

    // ---------- disable ----------

    @Test
    void disable_clearsFlavor_onClassAndInheritingDescendants() throws ServiceException {
        twinClass.setFlavorDataListId(UUID.randomUUID()); // parent had a flavor -> parentHadFlavor
        UUID childId = UUID.randomUUID();
        TwinClassEntity child = new TwinClassEntity().setId(childId)
                .setInheritedFlavorDataListTwinClassId(classId); // inherits flavor FROM this class
        when(twinClassRepository.findAll(any(Specification.class))).thenReturn(List.of(child));

        twinFlavorService.replaceFlavorForTwinsOfClass(twinClass, op(UuidUtils.NULLIFY_MARKER, EntityRelinkOperationStrategy.delete, null));

        verify(twinRepository).clearFlavorForTwinsOfClassIn(argThat((Collection<UUID> s) -> s.containsAll(List.of(classId, childId))));
        assertNull(twinClass.getFlavorDataListId());
    }

    @Test
    void disable_skipsDescendantWithItsOwnFlavorList() throws ServiceException {
        twinClass.setFlavorDataListId(UUID.randomUUID());
        UUID childId = UUID.randomUUID();
        TwinClassEntity child = new TwinClassEntity().setId(childId)
                .setFlavorDataListId(UUID.randomUUID()); // overrides inheritance -> not affected
        when(twinClassRepository.findAll(any(Specification.class))).thenReturn(List.of(child));

        twinFlavorService.replaceFlavorForTwinsOfClass(twinClass, op(UuidUtils.NULLIFY_MARKER, EntityRelinkOperationStrategy.delete, null));

        verify(twinRepository).clearFlavorForTwinsOfClassIn(argThat((Collection<UUID> s) -> s.contains(classId) && !s.contains(childId)));
    }

    // ---------- enable ----------

    @Test
    void enable_noTwins_setsList() throws ServiceException {
        UUID newList = UUID.randomUUID();
        UUID option = UUID.randomUUID();
        when(dataListService.findEntitySafe(newList)).thenReturn(dataList(newList, option));

        twinFlavorService.replaceFlavorForTwinsOfClass(twinClass, op(newList, EntityRelinkOperationStrategy.delete, null));

        assertEquals(newList, twinClass.getFlavorDataListId());
        verify(twinRepository, never()).setFlavorForTwinsWithoutFlavorIn(any(), any());
        verify(twinRepository, never()).deleteTwinsByTwinClassIdInAndFlavorDataListOptionIdIn(any(), any());
    }

    @Test
    void enable_flavorLessTwins_withNullifyDefault_backfills() throws ServiceException {
        UUID newList = UUID.randomUUID();
        UUID option = UUID.randomUUID();
        when(dataListService.findEntitySafe(newList)).thenReturn(dataList(newList, option));
        when(twinRepository.countByTwinClassIdInAndFlavorDataListOptionIdIsNull(any())).thenReturn(5L);

        twinFlavorService.replaceFlavorForTwinsOfClass(twinClass,
                op(newList, EntityRelinkOperationStrategy.delete, Map.of(UuidUtils.NULLIFY_MARKER, option)));

        verify(twinRepository).setFlavorForTwinsWithoutFlavorIn(argThat((Collection<UUID> s) -> s.contains(classId)), eq(option));
        assertEquals(newList, twinClass.getFlavorDataListId());
    }

    @Test
    void enable_flavorLessTwins_noDefault_throws() throws ServiceException {
        UUID newList = UUID.randomUUID();
        UUID option = UUID.randomUUID();
        when(dataListService.findEntitySafe(newList)).thenReturn(dataList(newList, option));
        when(twinRepository.countByTwinClassIdInAndFlavorDataListOptionIdIsNull(any())).thenReturn(3L);

        ServiceException ex = assertThrows(ServiceException.class, () ->
                twinFlavorService.replaceFlavorForTwinsOfClass(twinClass,
                        op(newList, EntityRelinkOperationStrategy.delete, null)));
        assertTrue(ex.getMessage().contains("flavor is mandatory"));
        verify(twinRepository, never()).setFlavorForTwinsWithoutFlavorIn(any(), any());
    }

    // ---------- change ----------

    @Test
    void change_optionKeptByNewList_noMigration() throws ServiceException {
        UUID oldList = UUID.randomUUID();
        UUID newList = UUID.randomUUID();
        UUID kept = UUID.randomUUID();
        twinClass.setFlavorDataListId(oldList);
        when(dataListService.findEntitySafe(newList)).thenReturn(dataList(newList, kept));
        when(twinRepository.findDistinctFlavorDataListOptionIdByTwinClassIdIn(any())).thenReturn(Set.of(kept));

        twinFlavorService.replaceFlavorForTwinsOfClass(twinClass, op(newList, EntityRelinkOperationStrategy.delete, null));

        verify(twinRepository, never()).replaceFlavorForTwinsOfClassIn(any(), any(), any());
        verify(twinRepository, never()).deleteTwinsByTwinClassIdInAndFlavorDataListOptionIdIn(any(), any());
        assertEquals(newList, twinClass.getFlavorDataListId());
    }

    @Test
    void change_mappedOption_replaces() throws ServiceException {
        UUID oldList = UUID.randomUUID();
        UUID newList = UUID.randomUUID();
        UUID oldOption = UUID.randomUUID();
        UUID newOption = UUID.randomUUID();
        twinClass.setFlavorDataListId(oldList);
        when(dataListService.findEntitySafe(newList)).thenReturn(dataList(newList, newOption));
        when(twinRepository.findDistinctFlavorDataListOptionIdByTwinClassIdIn(any())).thenReturn(Set.of(oldOption));

        twinFlavorService.replaceFlavorForTwinsOfClass(twinClass,
                op(newList, EntityRelinkOperationStrategy.delete, Map.of(oldOption, newOption)));

        verify(twinRepository).replaceFlavorForTwinsOfClassIn(argThat((Collection<UUID> s) -> s.contains(classId)), eq(oldOption), eq(newOption));
    }

    @Test
    void change_invalidMapping_throws() throws ServiceException {
        UUID oldList = UUID.randomUUID();
        UUID newList = UUID.randomUUID();
        UUID oldOption = UUID.randomUUID();
        UUID bogus = UUID.randomUUID();
        twinClass.setFlavorDataListId(oldList);
        when(dataListService.findEntitySafe(newList)).thenReturn(dataList(newList)); // bogus not in new list
        when(twinRepository.findDistinctFlavorDataListOptionIdByTwinClassIdIn(any())).thenReturn(Set.of(oldOption));

        ServiceException ex = assertThrows(ServiceException.class, () ->
                twinFlavorService.replaceFlavorForTwinsOfClass(twinClass,
                        op(newList, EntityRelinkOperationStrategy.delete, Map.of(oldOption, bogus))));
        assertTrue(ex.getMessage().contains("not an option of the new flavor list"));
    }

    @Test
    void change_strategyRestrict_unmappedOption_throws() throws ServiceException {
        UUID oldList = UUID.randomUUID();
        UUID newList = UUID.randomUUID();
        UUID oldOption = UUID.randomUUID();
        UUID newOption = UUID.randomUUID();
        twinClass.setFlavorDataListId(oldList);
        when(dataListService.findEntitySafe(newList)).thenReturn(dataList(newList, newOption));
        when(twinRepository.findDistinctFlavorDataListOptionIdByTwinClassIdIn(any())).thenReturn(Set.of(oldOption));

        ServiceException ex = assertThrows(ServiceException.class, () ->
                twinFlavorService.replaceFlavorForTwinsOfClass(twinClass,
                        op(newList, EntityRelinkOperationStrategy.restrict, Map.of())));
        assertTrue(ex.getMessage().contains("restrict"));
        verify(twinRepository, never()).deleteTwinsByTwinClassIdInAndFlavorDataListOptionIdIn(any(), any());
    }

    @Test
    void change_strategyDelete_unmappedOption_deletesTwins() throws ServiceException {
        UUID oldList = UUID.randomUUID();
        UUID newList = UUID.randomUUID();
        UUID oldOption = UUID.randomUUID();
        UUID newOption = UUID.randomUUID();
        twinClass.setFlavorDataListId(oldList);
        when(dataListService.findEntitySafe(newList)).thenReturn(dataList(newList, newOption));
        when(twinRepository.findDistinctFlavorDataListOptionIdByTwinClassIdIn(any())).thenReturn(Set.of(oldOption));

        twinFlavorService.replaceFlavorForTwinsOfClass(twinClass,
                op(newList, EntityRelinkOperationStrategy.delete, Map.of()));

        verify(twinRepository).deleteTwinsByTwinClassIdInAndFlavorDataListOptionIdIn(argThat((Collection<UUID> s) -> s.contains(classId)), eq(Set.of(oldOption)));
        verify(twinRepository, never()).replaceFlavorForTwinsOfClassIn(any(), any(), any());
    }

    // ---------- cascade ----------

    @Test
    void cascade_change_migratesDescendantTwinsInheritedFromClass() throws ServiceException {
        UUID oldList = UUID.randomUUID();
        UUID newList = UUID.randomUUID();
        UUID oldOption = UUID.randomUUID();
        UUID newOption = UUID.randomUUID();
        twinClass.setFlavorDataListId(oldList);

        UUID childId = UUID.randomUUID();
        TwinClassEntity child = new TwinClassEntity().setId(childId)
                .setInheritedFlavorDataListTwinClassId(classId); // inherits from this class -> affected
        when(twinClassRepository.findAll(any(Specification.class))).thenReturn(List.of(child));
        when(dataListService.findEntitySafe(newList)).thenReturn(dataList(newList, newOption));
        when(twinRepository.findDistinctFlavorDataListOptionIdByTwinClassIdIn(any())).thenReturn(Set.of(oldOption));

        twinFlavorService.replaceFlavorForTwinsOfClass(twinClass,
                op(newList, EntityRelinkOperationStrategy.delete, Map.of(oldOption, newOption)));

        // one batched replace covering both the parent and the descendant (no per-class fanout)
        verify(twinRepository).replaceFlavorForTwinsOfClassIn(
                argThat((Collection<UUID> s) -> s.containsAll(List.of(classId, childId))), eq(oldOption), eq(newOption));
    }

    @Test
    void cascade_skipsDescendantInheritingFromElsewhere() throws ServiceException {
        UUID oldList = UUID.randomUUID();
        UUID newList = UUID.randomUUID();
        UUID oldOption = UUID.randomUUID();
        UUID newOption = UUID.randomUUID();
        twinClass.setFlavorDataListId(oldList);

        UUID otherAncestor = UUID.randomUUID();
        UUID childId = UUID.randomUUID();
        TwinClassEntity child = new TwinClassEntity().setId(childId)
                .setInheritedFlavorDataListTwinClassId(otherAncestor); // inherits from a different ancestor
        when(twinClassRepository.findAll(any(Specification.class))).thenReturn(List.of(child));
        when(dataListService.findEntitySafe(newList)).thenReturn(dataList(newList, newOption));
        when(twinRepository.findDistinctFlavorDataListOptionIdByTwinClassIdIn(any())).thenReturn(Set.of(oldOption));

        twinFlavorService.replaceFlavorForTwinsOfClass(twinClass,
                op(newList, EntityRelinkOperationStrategy.delete, Map.of(oldOption, newOption)));

        verify(twinRepository).replaceFlavorForTwinsOfClassIn(
                argThat((Collection<UUID> s) -> s.contains(classId) && !s.contains(childId)), eq(oldOption), eq(newOption));
    }

    @Test
    void cascade_enable_backfillsDescendantFlavorLessTwins() throws ServiceException {
        // parent had NO flavor before -> parentHadFlavor=false; a descendant with no flavor source
        // will inherit from the parent once it is enabled -> it must be back-filled too.
        UUID newList = UUID.randomUUID();
        UUID option = UUID.randomUUID();
        UUID childId = UUID.randomUUID();
        TwinClassEntity child = new TwinClassEntity().setId(childId)
                .setInheritedFlavorDataListTwinClassId(null);
        when(twinClassRepository.findAll(any(Specification.class))).thenReturn(List.of(child));
        when(dataListService.findEntitySafe(newList)).thenReturn(dataList(newList, option));
        when(twinRepository.countByTwinClassIdInAndFlavorDataListOptionIdIsNull(any())).thenReturn(7L);

        twinFlavorService.replaceFlavorForTwinsOfClass(twinClass,
                op(newList, EntityRelinkOperationStrategy.delete, Map.of(UuidUtils.NULLIFY_MARKER, option)));

        verify(twinRepository).setFlavorForTwinsWithoutFlavorIn(
                argThat((Collection<UUID> s) -> s.containsAll(List.of(classId, childId))), eq(option));
        assertEquals(newList, twinClass.getFlavorDataListId());
    }

    @Test
    void change_mappedToNullifyMarker_deletesTwins() throws ServiceException {
        // explicit old -> NULLIFY_MARKER mapping requests deletion of the holding twins
        // (honored under either strategy, mirroring marker)
        UUID oldList = UUID.randomUUID();
        UUID newList = UUID.randomUUID();
        UUID oldOption = UUID.randomUUID();
        UUID keptOption = UUID.randomUUID();
        twinClass.setFlavorDataListId(oldList);
        when(dataListService.findEntitySafe(newList)).thenReturn(dataList(newList, keptOption));
        when(twinRepository.findDistinctFlavorDataListOptionIdByTwinClassIdIn(any())).thenReturn(Set.of(oldOption));

        twinFlavorService.replaceFlavorForTwinsOfClass(twinClass,
                op(newList, EntityRelinkOperationStrategy.restrict, Map.of(oldOption, UuidUtils.NULLIFY_MARKER)));

        verify(twinRepository).deleteTwinsByTwinClassIdInAndFlavorDataListOptionIdIn(
                argThat((Collection<UUID> s) -> s.contains(classId)), eq(Set.of(oldOption)));
        verify(twinRepository, never()).replaceFlavorForTwinsOfClassIn(any(), any(), any());
    }

    @Test
    void change_combined_replaceDeleteAndBackfill_inOnePass() throws ServiceException {
        UUID oldList = UUID.randomUUID();
        UUID newList = UUID.randomUUID();
        UUID mappedOld = UUID.randomUUID();
        UUID unmappedOld = UUID.randomUUID();
        UUID newOption = UUID.randomUUID();
        UUID defaultOption = UUID.randomUUID();
        twinClass.setFlavorDataListId(oldList);
        when(dataListService.findEntitySafe(newList)).thenReturn(dataList(newList, newOption, defaultOption));
        when(twinRepository.findDistinctFlavorDataListOptionIdByTwinClassIdIn(any()))
                .thenReturn(Set.of(mappedOld, unmappedOld));
        when(twinRepository.countByTwinClassIdInAndFlavorDataListOptionIdIsNull(any())).thenReturn(2L);

        twinFlavorService.replaceFlavorForTwinsOfClass(twinClass, op(newList,
                EntityRelinkOperationStrategy.delete,
                Map.of(mappedOld, newOption, UuidUtils.NULLIFY_MARKER, defaultOption)));

        verify(twinRepository).replaceFlavorForTwinsOfClassIn(any(), eq(mappedOld), eq(newOption));
        verify(twinRepository).deleteTwinsByTwinClassIdInAndFlavorDataListOptionIdIn(any(), eq(Set.of(unmappedOld)));
        verify(twinRepository).setFlavorForTwinsWithoutFlavorIn(any(), eq(defaultOption));
    }
}
