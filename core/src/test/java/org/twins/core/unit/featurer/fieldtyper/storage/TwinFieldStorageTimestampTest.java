package org.twins.core.unit.featurer.fieldtyper.storage;

import org.cambium.common.kit.Kit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinFieldTimestampEntity;
import org.twins.core.dao.twin.TwinFieldTimestampRepository;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.featurer.fieldtyper.storage.TwinFieldStorageSpirit;
import org.twins.core.featurer.fieldtyper.storage.TwinFieldStorageTimestamp;
import org.twins.core.service.twin.TwinService;
import org.twins.core.service.twinclass.TwinClassFieldService;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TwinFieldStorageTimestampTest extends BaseUnitTest {

    @Mock
    private TwinFieldTimestampRepository twinFieldTimestampRepository;
    @Mock
    private TwinService twinService;

    @Mock
    private TwinClassFieldService twinClassFieldService;


    private UUID fieldId;

    @BeforeEach
    void setUp() {
        fieldId = UUID.randomUUID();
    }

    private TwinEntity twin(UUID id) {
        var t = new TwinEntity();
        t.setId(id);
        t.setTwinClass(new TwinClassEntity().setTwinClassFieldKit(Kit.EMPTY));
        return t;
    }

    private TwinFieldTimestampEntity field(UUID twinId, UUID classFieldId) {
        var e = new TwinFieldTimestampEntity();
        e.setId(UUID.randomUUID());
        e.setTwinId(twinId);
        e.setTwinClassFieldId(classFieldId);
        return e;
    }

    private TwinFieldStorageTimestamp loadableStorage() {
        var storage = new TwinFieldStorageTimestamp(twinFieldTimestampRepository);
        setBaseField(storage, "twinService", twinService);
        setBaseField(storage, "twinClassFieldService", twinClassFieldService);
        return storage;
    }

    private static void setBaseField(Object target, String name, Object value) {
        try {
            Class<?> clazz = target.getClass();
            while (clazz != null) {
                try {
                    var field = clazz.getDeclaredField(name);
                    field.setAccessible(true);
                    field.set(target, value);
                    return;
                } catch (NoSuchFieldException e) {
                    clazz = clazz.getSuperclass();
                }
            }
            throw new NoSuchFieldException(name);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    @Nested
    class Load {

        @Test
        void load_groupsByTwinIdAndPopulatesPerTwinTimestampKit() throws org.cambium.common.exception.ServiceException {
            var storage = loadableStorage();
            var t1 = twin(UUID.randomUUID());
            var kit = new Kit<>(List.of(t1), TwinEntity::getId);

            when(twinFieldTimestampRepository.findByTwinIdIn(anyCollection()))
                    .thenReturn(List.of(field(t1.getId(), fieldId)));

            storage.load(kit);

            assertNotNull(t1.getTwinFieldTimestampKit());
            assertNotNull(t1.getTwinFieldTimestampKit().get(fieldId));
        }

        @Test
        void load_absentTwin_isInitialisedWithEmptyKit() throws org.cambium.common.exception.ServiceException {
            var storage = loadableStorage();
            var t1 = twin(UUID.randomUUID());
            var kit = new Kit<>(List.of(t1), TwinEntity::getId);

            when(twinFieldTimestampRepository.findByTwinIdIn(anyCollection()))
                    .thenReturn(List.of());

            storage.load(kit);

            // initEmpty now sets a mutable empty Kit (not the immutable Kit.EMPTY singleton) so field serialization can add to it.
            assertNotNull(t1.getTwinFieldTimestampKit());
            assertTrue(t1.getTwinFieldTimestampKit().isEmpty());
        }
    }

    @Nested
    class StrictValues {

        @Test
        void hasStrictValues_delegatesToRepository() {
            var f = UUID.randomUUID();
            when(twinFieldTimestampRepository.existsByTwinClassFieldId(f)).thenReturn(true);

            assertTrue(loadableStorage().hasStrictValues(f));
        }
    }

    @Nested
    class LoadState {

        @Test
        void isLoaded_reflectsTimestampKitPresence() {
            var storage = loadableStorage();
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
            when(twinFieldTimestampRepository.findUsedFieldsByTwinClassIdAndTwinClassFieldIdIn(classId, fields))
                    .thenReturn(used);

            assertEquals(used,
                    loadableStorage().findUsedFields(classId, fields));
        }

        @Test
        void replaceTwinClassFieldForTwinsOfClass_delegatesToRepository() {
            var storage = loadableStorage();
            var classId = UUID.randomUUID();
            var from = UUID.randomUUID();
            var to = UUID.randomUUID();

            storage.replaceTwinClassFieldForTwinsOfClass(classId, from, to);

            verify(twinFieldTimestampRepository).replaceTwinClassFieldForTwinsOfClass(classId, from, to);
        }

        @Test
        void deleteTwinFieldsForTwins_delegatesPerEntry() {
            var storage = loadableStorage();
            var twinId = UUID.randomUUID();
            var fields = Set.<UUID>of(UUID.randomUUID());

            storage.deleteTwinFieldsForTwins(Map.of(twinId, fields));

            verify(twinFieldTimestampRepository).deleteByTwinIdAndTwinClassFieldIdIn(twinId, fields);
        }
    }

    @Nested
    class EqualsAndMerge {

        @Test
        void equals_twoInstancesOfSameClass_isTrue() {
            assertEquals(
                    loadableStorage(),
                    loadableStorage());
        }

        @Test
        void equals_differentStorageClass_isFalse() {
            assertNotEquals(loadableStorage(), new TwinFieldStorageSpirit());
        }
    }
}
