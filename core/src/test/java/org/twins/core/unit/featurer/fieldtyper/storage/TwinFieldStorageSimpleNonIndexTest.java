package org.twins.core.unit.featurer.fieldtyper.storage;
import org.twins.core.featurer.fieldtyper.storage.TwinFieldStorageSpirit;

import org.cambium.common.kit.Kit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinFieldSimpleNonIndexedEntity;
import org.twins.core.dao.twin.TwinFieldSimpleNonIndexedRepository;
import org.twins.core.featurer.fieldtyper.storage.TwinFieldStorageSimpleNonIndex;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TwinFieldStorageSimpleNonIndexTest extends BaseUnitTest {

    @Mock
    private TwinFieldSimpleNonIndexedRepository twinFieldSimpleNonIndexedRepository;

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

    private TwinFieldSimpleNonIndexedEntity field(UUID twinId, UUID classFieldId) {
        var e = new TwinFieldSimpleNonIndexedEntity();
        e.setId(UUID.randomUUID());
        e.setTwinId(twinId);
        e.setTwinClassFieldId(classFieldId);
        return e;
    }

    @Nested
    class Load {

        @Test
        void load_groupsByTwinIdAndPopulatesPerTwinNonIndexedKit() {
            var storage = new TwinFieldStorageSimpleNonIndex(twinFieldSimpleNonIndexedRepository);
            var t1 = twin(UUID.randomUUID());
            var kit = new Kit<>(List.of(t1), TwinEntity::getId);

            when(twinFieldSimpleNonIndexedRepository.findByTwinIdIn(kit.getIdSet()))
                    .thenReturn(List.of(field(t1.getId(), fieldId)));

            storage.load(kit);

            assertNotNull(t1.getTwinFieldSimpleNonIndexedKit());
            assertNotNull(t1.getTwinFieldSimpleNonIndexedKit().get(fieldId));
        }

        @Test
        void load_absentTwin_isInitialisedWithEmptyKit() {
            var storage = new TwinFieldStorageSimpleNonIndex(twinFieldSimpleNonIndexedRepository);
            var t1 = twin(UUID.randomUUID());
            var kit = new Kit<>(List.of(t1), TwinEntity::getId);

            when(twinFieldSimpleNonIndexedRepository.findByTwinIdIn(kit.getIdSet()))
                    .thenReturn(List.of());

            storage.load(kit);

            assertEquals(Kit.EMPTY, t1.getTwinFieldSimpleNonIndexedKit());
        }
    }

    @Nested
    class StrictValues {

        @Test
        void hasStrictValues_delegatesToRepository() {
            var f = UUID.randomUUID();
            when(twinFieldSimpleNonIndexedRepository.existsByTwinClassFieldId(f)).thenReturn(true);

            assertTrue(new TwinFieldStorageSimpleNonIndex(twinFieldSimpleNonIndexedRepository).hasStrictValues(f));
        }
    }

    @Nested
    class LoadState {

        @Test
        void isLoaded_reflectsNonIndexedKitPresence() {
            var storage = new TwinFieldStorageSimpleNonIndex(twinFieldSimpleNonIndexedRepository);
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
            when(twinFieldSimpleNonIndexedRepository.findUsedFieldsByTwinClassIdAndTwinClassFieldIdIn(classId, fields))
                    .thenReturn(used);

            assertEquals(used,
                    new TwinFieldStorageSimpleNonIndex(twinFieldSimpleNonIndexedRepository).findUsedFields(classId, fields));
        }

        @Test
        void replaceTwinClassFieldForTwinsOfClass_delegatesToRepository() {
            var storage = new TwinFieldStorageSimpleNonIndex(twinFieldSimpleNonIndexedRepository);
            var classId = UUID.randomUUID();
            var from = UUID.randomUUID();
            var to = UUID.randomUUID();

            storage.replaceTwinClassFieldForTwinsOfClass(classId, from, to);

            verify(twinFieldSimpleNonIndexedRepository).replaceTwinClassFieldForTwinsOfClass(classId, from, to);
        }

        @Test
        void deleteTwinFieldsForTwins_delegatesPerEntry() {
            var storage = new TwinFieldStorageSimpleNonIndex(twinFieldSimpleNonIndexedRepository);
            var twinId = UUID.randomUUID();
            var fields = Set.<UUID>of(UUID.randomUUID());

            storage.deleteTwinFieldsForTwins(Map.of(twinId, fields));

            verify(twinFieldSimpleNonIndexedRepository).deleteByTwinIdAndTwinClassFieldIdIn(twinId, fields);
        }
    }

    @Nested
    class EqualsAndMerge {

        @Test
        void equals_twoInstancesOfSameClass_isTrue() {
            assertEquals(
                    new TwinFieldStorageSimpleNonIndex(twinFieldSimpleNonIndexedRepository),
                    new TwinFieldStorageSimpleNonIndex(twinFieldSimpleNonIndexedRepository));
        }

        @Test
        void equals_differentStorageClass_isFalse() {
            assertNotEquals(
                    new TwinFieldStorageSimpleNonIndex(twinFieldSimpleNonIndexedRepository),
                    new TwinFieldStorageSpirit());
        }
    }
}
