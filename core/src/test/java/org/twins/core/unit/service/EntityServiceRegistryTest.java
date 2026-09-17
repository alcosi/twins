package org.twins.core.unit.service;

import org.cambium.common.exception.ServiceException;
import org.cambium.common.kit.DuplicateKeyMode;
import org.cambium.common.kit.Kit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.twins.core.dao.twin.TwinPointerEntity;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.mappers.rest.mappercontext.EntityRef;
import org.twins.core.service.EntityServiceRegistry;
import org.twins.core.service.datalist.DataListSubsetService;
import org.twins.core.service.twinclass.TwinClassService;
import org.twins.core.service.twinpointer.TwinPointerService;

import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.Mockito.*;

/**
 * EntityServiceRegistry: entity class -> EntitySecureFindServiceImpl lookup, strict bulk-load delegation
 * and the EntityRef pattern load (needLoad filter, one query per entity class, distribute via setters).
 */
public class EntityServiceRegistryTest {

    private final TwinClassService twinClassService = mock(TwinClassService.class);
    private final TwinPointerService twinPointerService = mock(TwinPointerService.class);
    private final DataListSubsetService dataListSubsetService = mock(DataListSubsetService.class);
    private final EntityServiceRegistry entityServiceRegistry = new EntityServiceRegistry(
            twinClassService, null, null, null, null, null, null, null, null,
            dataListSubsetService, twinPointerService, null, null, null, null, null, null, null, null);

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.invokeMethod(entityServiceRegistry, "initRegistry");
    }

    @Test
    public void resolvesServiceByEntityClass() {
        assertEquals(twinClassService, entityServiceRegistry.getService(TwinClassEntity.class));
        assertEquals(twinPointerService, entityServiceRegistry.getService(TwinPointerEntity.class));
        assertNull(entityServiceRegistry.getService(Object.class)); // not registered
    }

    @Test
    public void delegatesBulkLoadToTheServiceOfTheClass() throws ServiceException {
        Kit<TwinClassEntity, UUID> kit = new Kit<>(TwinClassEntity::getId, DuplicateKeyMode.IGNORE);
        when(twinClassService.findEntitiesSafe(anySet())).thenReturn(kit);

        assertEquals(kit, entityServiceRegistry.findEntitiesSafe(TwinClassEntity.class, Set.of(UUID.randomUUID())));

        verify(twinClassService, times(1)).findEntitiesSafe(anySet());
        verifyNoInteractions(twinPointerService, dataListSubsetService);
    }

    @Test
    public void unknownClassReturnsNullWithoutFailure() throws ServiceException {
        assertNull(entityServiceRegistry.findEntitiesSafe(Object.class, Set.of(UUID.randomUUID())));
        verifyNoInteractions(twinClassService, twinPointerService, dataListSubsetService);
    }

    @Test
    public void loadFillsRefsWithOneQueryPerEntityClass() throws ServiceException {
        UUID twinClassId1 = UUID.randomUUID();
        UUID twinClassId2 = UUID.randomUUID();
        UUID twinPointerId = UUID.randomUUID();
        TwinClassEntity twinClass1 = new TwinClassEntity().setId(twinClassId1);
        TwinClassEntity twinClass2 = new TwinClassEntity().setId(twinClassId2);
        TwinPointerEntity twinPointer = new TwinPointerEntity().setId(twinPointerId);
        when(twinClassService.findEntitiesSafe(anySet())).thenReturn(kitOf(TwinClassEntity::getId, twinClass1, twinClass2));
        when(twinPointerService.findEntitiesSafe(anySet())).thenReturn(kitOf(TwinPointerEntity::getId, twinPointer));

        EntityRef ref1 = new EntityRef(TwinClassEntity.class, twinClassId1);
        EntityRef ref2 = new EntityRef(TwinClassEntity.class, twinClassId2);
        EntityRef ref3 = new EntityRef(TwinPointerEntity.class, twinPointerId);
        entityServiceRegistry.load(java.util.List.of(ref1, ref2, ref3));

        verify(twinClassService, times(1)).findEntitiesSafe(anySet()); // one bulk query per class
        verify(twinPointerService, times(1)).findEntitiesSafe(anySet());
        assertEquals(twinClass1, ref1.getEntity()); // loaded entities distributed via setters
        assertEquals(twinClass2, ref2.getEntity());
        assertEquals(twinPointer, ref3.getEntity());
    }

    @Test
    public void loadSkipsAlreadyLoadedRefs() throws ServiceException {
        UUID twinClassId = UUID.randomUUID();
        TwinClassEntity alreadyLoaded = new TwinClassEntity().setId(twinClassId);
        EntityRef ref = new EntityRef(TwinClassEntity.class, twinClassId);
        ref.setEntity(alreadyLoaded); // needLoad filter must skip it

        entityServiceRegistry.load(java.util.List.of(ref));

        verifyNoInteractions(twinClassService);
        assertEquals(alreadyLoaded, ref.getEntity());
    }

    @Test
    public void loadSingleRefDelegatesToCollectionLoad() throws ServiceException {
        UUID twinClassId = UUID.randomUUID();
        TwinClassEntity twinClass = new TwinClassEntity().setId(twinClassId);
        when(twinClassService.findEntitiesSafe(anySet())).thenReturn(kitOf(TwinClassEntity::getId, twinClass));

        EntityRef ref = new EntityRef(TwinClassEntity.class, twinClassId);
        entityServiceRegistry.load(ref);

        assertEquals(twinClass, ref.getEntity());
    }

    @SafeVarargs
    private static <T> Kit<T, UUID> kitOf(java.util.function.Function<T, UUID> getId, T... entities) {
        Kit<T, UUID> kit = new Kit<>(getId, DuplicateKeyMode.IGNORE);
        for (T entity : entities)
            kit.add(entity);
        return kit;
    }
}
