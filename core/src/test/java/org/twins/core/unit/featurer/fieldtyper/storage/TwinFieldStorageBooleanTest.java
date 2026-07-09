package org.twins.core.unit.featurer.fieldtyper.storage;
import org.twins.core.featurer.fieldtyper.storage.TwinFieldStorageSpirit;

import org.cambium.common.kit.Kit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinFieldBooleanEntity;
import org.twins.core.dao.twin.TwinFieldBooleanRepository;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.featurer.fieldtyper.storage.TwinFieldStorageBoolean;
import org.twins.core.service.twin.TwinService;
import org.twins.core.service.twinclass.TwinClassFieldService;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TwinFieldStorageBooleanTest extends BaseUnitTest {

    @Mock
    private TwinFieldBooleanRepository twinFieldBooleanRepository;

    @Mock
    private TwinService twinService;

    @Mock
    private TwinClassFieldService twinClassFieldService;

    private UUID fieldId;

    @BeforeEach
    void setUp() {
        fieldId = UUID.randomUUID();
    }

    // TwinFieldStorageMater.load() uses @Autowired twinService/twinClassFieldService — inject them for load tests.
    private TwinFieldStorageBoolean loadableStorage() {
        var storage = new TwinFieldStorageBoolean(twinFieldBooleanRepository);
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

    private TwinEntity twin(UUID id) {
        var t = new TwinEntity();
        t.setId(id);
        t.setTwinClass(new TwinClassEntity().setTwinClassFieldKit(Kit.EMPTY));
        return t;
    }

    private TwinFieldBooleanEntity field(UUID twinId, UUID classFieldId) {
        var e = new TwinFieldBooleanEntity();
        e.setId(UUID.randomUUID());
        e.setTwinId(twinId);
        e.setTwinClassFieldId(classFieldId);
        return e;
    }

    @Nested
    class Load {

        @Test
        void load_groupsByTwinIdAndPopulatesPerTwinKit() throws org.cambium.common.exception.ServiceException {
            var storage = loadableStorage();
            var t1 = twin(UUID.randomUUID());
            var t2 = twin(UUID.randomUUID());
            var kit = new Kit<>(java.util.Arrays.asList(t1, t2), TwinEntity::getId);

            when(twinFieldBooleanRepository.findByTwinIdIn(anyCollection()))
                    .thenReturn(List.of(
                            field(t1.getId(), fieldId),
                            field(t2.getId(), UUID.randomUUID())));

            storage.load(kit);

            // Per-twin kit keyed by twinClassFieldId, present on each twin that had rows.
            assertNotNull(t1.getTwinFieldBooleanKit());
            assertNotNull(t1.getTwinFieldBooleanKit().get(fieldId));
            assertNotNull(t2.getTwinFieldBooleanKit());
        }

        @Test
        void load_twinWithoutRows_isInitialisedEmptyViaInitEmpty() throws org.cambium.common.exception.ServiceException {
            var storage = loadableStorage();
            var present = twin(UUID.randomUUID());
            var absent = twin(UUID.randomUUID());
            var kit = new Kit<>(java.util.Arrays.asList(present, absent), TwinEntity::getId);

            when(twinFieldBooleanRepository.findByTwinIdIn(anyCollection()))
                    .thenReturn(List.of(field(present.getId(), fieldId)));

            storage.load(kit);

            // Absent twin must still be marked loaded (initEmpty sets Kit.EMPTY).
            assertNotNull(present.getTwinFieldBooleanKit());
            assertEquals(Kit.EMPTY, absent.getTwinFieldBooleanKit());
        }
    }

    @Nested
    class StrictValues {

        @Test
        void hasStrictValues_delegatesToRepositoryExistsCheck() {
            var fieldId = UUID.randomUUID();
            when(twinFieldBooleanRepository.existsByTwinClassFieldId(fieldId)).thenReturn(true);

            assertTrue(new TwinFieldStorageBoolean(twinFieldBooleanRepository).hasStrictValues(fieldId));
        }

        @Test
        void hasStrictValues_repoSaysNo_returnsFalse() {
            when(twinFieldBooleanRepository.existsByTwinClassFieldId(any(UUID.class))).thenReturn(false);

            assertFalse(new TwinFieldStorageBoolean(twinFieldBooleanRepository).hasStrictValues(UUID.randomUUID()));
        }
    }

    @Nested
    class LoadState {

        @Test
        void isLoaded_nullKit_isFalse() {
            assertFalse(new TwinFieldStorageBoolean(twinFieldBooleanRepository).isLoaded(twin(UUID.randomUUID())));
        }

        @Test
        void isLoaded_afterInitEmpty_isTrue() {
            var storage = new TwinFieldStorageBoolean(twinFieldBooleanRepository);
            var t = twin(UUID.randomUUID());

            storage.initEmpty(t);

            assertTrue(storage.isLoaded(t));
            assertEquals(Kit.EMPTY, t.getTwinFieldBooleanKit());
        }
    }

    @Nested
    class DelegatingOperations {

        @Test
        void findUsedFields_delegatesToRepository() {
            var classId = UUID.randomUUID();
            var fields = Set.<UUID>of(UUID.randomUUID());
            var used = List.of(UUID.randomUUID());
            when(twinFieldBooleanRepository.findUsedFieldsByTwinClassIdAndTwinClassFieldIdIn(classId, fields))
                    .thenReturn(used);

            assertEquals(used,
                    new TwinFieldStorageBoolean(twinFieldBooleanRepository).findUsedFields(classId, fields));
        }

        @Test
        void replaceTwinClassFieldForTwinsOfClass_delegatesToRepository() {
            var storage = new TwinFieldStorageBoolean(twinFieldBooleanRepository);
            var classId = UUID.randomUUID();
            var from = UUID.randomUUID();
            var to = UUID.randomUUID();

            storage.replaceTwinClassFieldForTwinsOfClass(classId, from, to);

            verify(twinFieldBooleanRepository).replaceTwinClassFieldForTwinsOfClass(classId, from, to);
        }

        @Test
        void deleteTwinFieldsForTwins_iteratesMapAndDeletesPerTwin() {
            var storage = new TwinFieldStorageBoolean(twinFieldBooleanRepository);
            var twinId1 = UUID.randomUUID();
            var twinId2 = UUID.randomUUID();
            var fields1 = Set.<UUID>of(UUID.randomUUID());
            var fields2 = Set.<UUID>of(UUID.randomUUID());

            storage.deleteTwinFieldsForTwins(Map.of(twinId1, fields1, twinId2, fields2));

            verify(twinFieldBooleanRepository).deleteByTwinIdAndTwinClassFieldIdIn(twinId1, fields1);
            verify(twinFieldBooleanRepository).deleteByTwinIdAndTwinClassFieldIdIn(twinId2, fields2);
        }
    }

    @Nested
    class EqualsAndMerge {

        @Test
        void equals_twoInstancesOfSameClass_isTrue() {
            assertEquals(
                    new TwinFieldStorageBoolean(twinFieldBooleanRepository),
                    new TwinFieldStorageBoolean(twinFieldBooleanRepository));
        }

        @Test
        void equals_differentStorageClass_isFalse() {
            assertNotEquals(
                    new TwinFieldStorageBoolean(twinFieldBooleanRepository),
                    new TwinFieldStorageSpirit());
        }
    }
}
