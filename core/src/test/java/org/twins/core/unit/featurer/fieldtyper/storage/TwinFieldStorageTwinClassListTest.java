package org.twins.core.unit.featurer.fieldtyper.storage;
import org.twins.core.featurer.fieldtyper.storage.TwinFieldStorageSpirit;

import org.cambium.common.kit.Kit;
import org.cambium.common.kit.KitGrouped;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinFieldTwinClassEntity;
import org.twins.core.dao.twin.TwinFieldTwinClassListRepository;
import org.twins.core.featurer.fieldtyper.storage.TwinFieldStorageTwinClassList;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TwinFieldStorageTwinClassListTest extends BaseUnitTest {

    @Mock
    private TwinFieldTwinClassListRepository twinFieldTwinClassListRepository;

    private UUID fieldId;

    @BeforeEach
    void setUp() {
        fieldId = UUID.randomUUID();
    }

    private TwinEntity twin(UUID id) {
        var t = new TwinEntity();
        t.setId(id);
        return t;
    }

    private TwinFieldTwinClassEntity field(UUID twinId, UUID classFieldId) {
        var e = new TwinFieldTwinClassEntity();
        e.setId(UUID.randomUUID());
        e.setTwinId(twinId);
        e.setTwinClassFieldId(classFieldId);
        return e;
    }

    @Nested
    class Load {

        @Test
        void load_groupsByTwinIdAndPopulatesPerTwinTwinClassKit() {
            var storage = new TwinFieldStorageTwinClassList(twinFieldTwinClassListRepository);
            var t1 = twin(UUID.randomUUID());
            var kit = new Kit<>(List.of(t1), TwinEntity::getId);

            when(twinFieldTwinClassListRepository.findByTwinIdIn(kit.getIdSet()))
                    .thenReturn(List.of(field(t1.getId(), fieldId)));

            storage.load(kit);

            assertNotNull(t1.getTwinFieldTwinClassKit());
            assertTrue(t1.getTwinFieldTwinClassKit().containsGroupedKey(fieldId));
        }

        @Test
        void load_absentTwin_isInitialisedWithEmptyKit() {
            var storage = new TwinFieldStorageTwinClassList(twinFieldTwinClassListRepository);
            var t1 = twin(UUID.randomUUID());
            var kit = new Kit<>(List.of(t1), TwinEntity::getId);

            when(twinFieldTwinClassListRepository.findByTwinIdIn(kit.getIdSet()))
                    .thenReturn(List.of());

            storage.load(kit);

            assertEquals(KitGrouped.EMPTY, t1.getTwinFieldTwinClassKit());
        }
    }

    @Nested
    class StrictValues {

        @Test
        void hasStrictValues_delegatesToRepository() {
            var f = UUID.randomUUID();
            when(twinFieldTwinClassListRepository.existsByTwinClassFieldId(f)).thenReturn(true);

            assertTrue(new TwinFieldStorageTwinClassList(twinFieldTwinClassListRepository).hasStrictValues(f));
        }
    }

    @Nested
    class LoadState {

        @Test
        void isLoaded_reflectsTwinClassKitPresence() {
            var storage = new TwinFieldStorageTwinClassList(twinFieldTwinClassListRepository);
            var t = twin(UUID.randomUUID());

            assertFalse(storage.isLoaded(t));

            storage.initEmpty(t);

            assertTrue(storage.isLoaded(t));
        }
    }

    @Nested
    class DelegatingOperations {

        @Test
        void findUsedFields_delegatesToRepository() {
            var classId = UUID.randomUUID();
            var fields = Set.<UUID>of(UUID.randomUUID());
            var used = List.of(UUID.randomUUID());
            when(twinFieldTwinClassListRepository.findUsedFieldsByTwinClassIdAndTwinClassFieldIdIn(classId, fields))
                    .thenReturn(used);

            assertEquals(used,
                    new TwinFieldStorageTwinClassList(twinFieldTwinClassListRepository).findUsedFields(classId, fields));
        }

        @Test
        void replaceTwinClassFieldForTwinsOfClass_delegatesToRepository() {
            var storage = new TwinFieldStorageTwinClassList(twinFieldTwinClassListRepository);
            var classId = UUID.randomUUID();
            var from = UUID.randomUUID();
            var to = UUID.randomUUID();

            storage.replaceTwinClassFieldForTwinsOfClass(classId, from, to);

            verify(twinFieldTwinClassListRepository).replaceTwinClassFieldForTwinsOfClass(classId, from, to);
        }

        @Test
        void deleteTwinFieldsForTwins_delegatesPerEntry() {
            var storage = new TwinFieldStorageTwinClassList(twinFieldTwinClassListRepository);
            var twinId = UUID.randomUUID();
            var fields = Set.<UUID>of(UUID.randomUUID());

            storage.deleteTwinFieldsForTwins(Map.of(twinId, fields));

            verify(twinFieldTwinClassListRepository).deleteByTwinIdAndTwinClassFieldIdIn(twinId, fields);
        }
    }

    @Nested
    class EqualsAndMerge {

        @Test
        void equals_twoInstancesOfSameClass_isTrue() {
            assertEquals(
                    new TwinFieldStorageTwinClassList(twinFieldTwinClassListRepository),
                    new TwinFieldStorageTwinClassList(twinFieldTwinClassListRepository));
        }

        @Test
        void equals_differentStorageClass_isFalse() {
            assertNotEquals(
                    new TwinFieldStorageTwinClassList(twinFieldTwinClassListRepository),
                    new TwinFieldStorageSpirit());
        }
    }
}
