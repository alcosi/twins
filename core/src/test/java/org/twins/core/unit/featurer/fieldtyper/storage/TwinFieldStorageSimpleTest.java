package org.twins.core.unit.featurer.fieldtyper.storage;
import org.twins.core.featurer.fieldtyper.storage.TwinFieldStorageSpirit;

import org.cambium.common.kit.Kit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinFieldSimpleEntity;
import org.twins.core.dao.twin.TwinFieldSimpleRepository;
import org.twins.core.featurer.fieldtyper.storage.TwinFieldStorageSimple;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TwinFieldStorageSimpleTest extends BaseUnitTest {

    @Mock
    private TwinFieldSimpleRepository twinFieldSimpleRepository;

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

    private TwinFieldSimpleEntity field(UUID twinId, UUID classFieldId) {
        var e = new TwinFieldSimpleEntity();
        e.setId(UUID.randomUUID());
        e.setTwinId(twinId);
        e.setTwinClassFieldId(classFieldId);
        return e;
    }

    @Nested
    class Load {

        @Test
        void load_groupsByTwinIdAndPopulatesPerTwinSimpleKit() {
            var storage = new TwinFieldStorageSimple(twinFieldSimpleRepository);
            var t1 = twin(UUID.randomUUID());
            var t2 = twin(UUID.randomUUID());
            var kit = new Kit<>(java.util.Arrays.asList(t1, t2), TwinEntity::getId);

            when(twinFieldSimpleRepository.findByTwinIdIn(kit.getIdSet()))
                    .thenReturn(List.of(field(t1.getId(), fieldId)));

            storage.load(kit);

            assertNotNull(t1.getTwinFieldSimpleKit());
            assertNotNull(t1.getTwinFieldSimpleKit().get(fieldId));
            // t2 had no rows -> initEmpty set Kit.EMPTY
            assertEquals(Kit.EMPTY, t2.getTwinFieldSimpleKit());
        }
    }

    @Nested
    class StrictValues {

        @Test
        void hasStrictValues_delegatesToRepository() {
            var f = UUID.randomUUID();
            when(twinFieldSimpleRepository.existsByTwinClassFieldId(f)).thenReturn(true);

            assertTrue(new TwinFieldStorageSimple(twinFieldSimpleRepository).hasStrictValues(f));
        }
    }

    @Nested
    class LoadState {

        @Test
        void isLoaded_reflectsSimpleKitPresence() {
            var storage = new TwinFieldStorageSimple(twinFieldSimpleRepository);
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
            when(twinFieldSimpleRepository.findUsedFieldsByTwinClassIdAndTwinClassFieldIdIn(classId, fields))
                    .thenReturn(used);

            assertEquals(used,
                    new TwinFieldStorageSimple(twinFieldSimpleRepository).findUsedFields(classId, fields));
        }

        @Test
        void replaceTwinClassFieldForTwinsOfClass_delegatesToRepository() {
            var storage = new TwinFieldStorageSimple(twinFieldSimpleRepository);
            var classId = UUID.randomUUID();
            var from = UUID.randomUUID();
            var to = UUID.randomUUID();

            storage.replaceTwinClassFieldForTwinsOfClass(classId, from, to);

            verify(twinFieldSimpleRepository).replaceTwinClassFieldForTwinsOfClass(classId, from, to);
        }

        @Test
        void deleteTwinFieldsForTwins_delegatesPerEntry() {
            var storage = new TwinFieldStorageSimple(twinFieldSimpleRepository);
            var twinId = UUID.randomUUID();
            var fields = Set.<UUID>of(UUID.randomUUID());

            storage.deleteTwinFieldsForTwins(Map.of(twinId, fields));

            verify(twinFieldSimpleRepository).deleteByTwinIdAndTwinClassFieldIdIn(twinId, fields);
        }
    }

    @Nested
    class EqualsAndMerge {

        @Test
        void equals_twoInstancesOfSameClass_isTrue() {
            assertEquals(
                    new TwinFieldStorageSimple(twinFieldSimpleRepository),
                    new TwinFieldStorageSimple(twinFieldSimpleRepository));
        }

        @Test
        void equals_differentStorageClass_isFalse() {
            assertNotEquals(new TwinFieldStorageSimple(twinFieldSimpleRepository), new TwinFieldStorageSpirit());
        }
    }
}
