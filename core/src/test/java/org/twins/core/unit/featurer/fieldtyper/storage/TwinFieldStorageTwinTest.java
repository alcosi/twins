package org.twins.core.unit.featurer.fieldtyper.storage;
import org.twins.core.featurer.fieldtyper.storage.TwinFieldStorageSpirit;

import org.cambium.common.exception.ServiceException;
import org.cambium.common.kit.Kit;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.featurer.fieldtyper.storage.TwinFieldStorageTwin;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class TwinFieldStorageTwinTest extends BaseUnitTest {

    private TwinEntity twin() {
        var t = new TwinEntity();
        t.setId(UUID.randomUUID());
        return t;
    }

    @Nested
    class NoopContract {

        @Test
        void load_isNoop_doesNotPopulateAnyFieldKit() throws ServiceException {
            // Contract: twin's own fields live on TwinEntity itself; storage has nothing to load.
            var storage = new TwinFieldStorageTwin();
            var t = twin();
            var kit = new Kit<>(List.of(t), TwinEntity::getId);

            storage.load(kit);

            assertNull(t.getTwinFieldSimpleKit());
            assertNull(t.getTwinFieldBooleanKit());
        }

        @Test
        void hasStrictValues_isAlwaysFalse() {
            assertFalse(new TwinFieldStorageTwin().hasStrictValues(UUID.randomUUID()));
        }

        @Test
        void isLoaded_isAlwaysTrue() {
            var storage = new TwinFieldStorageTwin();
            var t = new TwinEntity();

            assertTrue(storage.isLoaded(t));
        }

        @Test
        void initEmpty_isNoop_doesNotMutateTwin() {
            var storage = new TwinFieldStorageTwin();
            var t = new TwinEntity();

            storage.initEmpty(t);

            assertNull(t.getTwinFieldSimpleKit());
            assertNull(t.getTwinLinks());
        }

        @Test
        void findUsedFields_returnsEmptyList() {
            assertEquals(List.of(),
                    new TwinFieldStorageTwin().findUsedFields(UUID.randomUUID(), Set.of()));
        }

        @Test
        void replaceTwinClassFieldForTwinsOfClass_isNoop_noExceptionThrown() {
            // No collaborator; documented no-op. Must not throw.
            assertDoesNotThrow(
                    () -> new TwinFieldStorageTwin()
                            .replaceTwinClassFieldForTwinsOfClass(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID()));
        }

        @Test
        void deleteTwinFieldsForTwins_isNoop_noExceptionThrown() {
            assertDoesNotThrow(
                    () -> new TwinFieldStorageTwin().deleteTwinFieldsForTwins(java.util.Map.of()));
        }
    }

    @Nested
    class EqualsAndMerge {

        @Test
        void equals_twoInstancesOfSameClass_isTrue() {
            assertEquals(new TwinFieldStorageTwin(), new TwinFieldStorageTwin());
        }

        @Test
        void equals_differentClass_isFalse() {
            assertNotEquals(new TwinFieldStorageTwin(), new TwinFieldStorageSpirit());
        }
    }
}
