package org.twins.core.unit.featurer.fieldtyper.storage;

import org.cambium.common.exception.ServiceException;
import org.cambium.common.kit.Kit;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.featurer.fieldtyper.storage.TwinFieldStorageSpirit;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class TwinFieldStorageSpiritTest extends BaseUnitTest {

    private TwinEntity twin() {
        var t = new TwinEntity();
        t.setId(UUID.randomUUID());
        return t;
    }

    @Nested
    class NoopContract {

        @Test
        void load_isNoop_doesNotTouchTwins() throws ServiceException {
            // Spirit is a marker storage with nothing to load — calling load must not mutate twins.
            var storage = new TwinFieldStorageSpirit();
            var t = twin();
            var kit = new Kit<>(List.of(t), TwinEntity::getId);

            storage.load(kit);

            assertNull(t.getTwinFieldSimpleKit());
            assertNull(t.getTwinFieldBooleanKit());
            assertNull(t.getTwinFieldDecimalKit());
        }

        @Test
        void hasStrictValues_isAlwaysFalse() {
            assertFalse(new TwinFieldStorageSpirit().hasStrictValues(UUID.randomUUID()));
        }

        @Test
        void isLoaded_isAlwaysTrue_evenForFreshTwin() {
            // Spirit has no per-twin state, so "loaded" is trivially true regardless of entity.
            var storage = new TwinFieldStorageSpirit();
            var t = new TwinEntity();

            assertTrue(storage.isLoaded(t));
        }

        @Test
        void initEmpty_isNoop_doesNotMutateTwin() {
            var storage = new TwinFieldStorageSpirit();
            var t = new TwinEntity();

            storage.initEmpty(t);

            assertNull(t.getTwinFieldSimpleKit());
            assertNull(t.getTwinLinks());
        }

        @Test
        void findUsedFields_returnsEmptyList() {
            var storage = new TwinFieldStorageSpirit();

            assertEquals(List.of(), storage.findUsedFields(UUID.randomUUID(), Set.of()));
        }
    }

    @Nested
    class EqualsAndMerge {

        @Test
        void equals_twoInstancesOfSameClass_isTrue() {
            // canBeMerged only checks class identity -> any two Spirit instances merge.
            assertEquals(new TwinFieldStorageSpirit(), new TwinFieldStorageSpirit());
        }

        @Test
        void equals_differentStorageClass_isFalse() {
            var a = new TwinFieldStorageSpirit();
            var b = mock(org.twins.core.featurer.fieldtyper.storage.TwinFieldStorageTwin.class);

            assertNotEquals(a, b);
        }
    }
}
