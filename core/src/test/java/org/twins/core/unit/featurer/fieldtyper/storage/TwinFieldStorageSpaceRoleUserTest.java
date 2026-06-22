package org.twins.core.unit.featurer.fieldtyper.storage;

import org.cambium.common.kit.Kit;
import org.cambium.common.kit.KitGrouped;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.dao.space.SpaceRoleUserRepository;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.featurer.fieldtyper.storage.TwinFieldStorageSpaceRoleUser;
import org.twins.core.featurer.fieldtyper.storage.TwinFieldStorageSpirit;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TwinFieldStorageSpaceRoleUserTest extends BaseUnitTest {

    @Mock
    private SpaceRoleUserRepository spaceRoleUserRepository;

    private TwinEntity twin(UUID id, UUID permissionSchemaSpaceId) {
        var t = new TwinEntity();
        t.setId(id);
        t.setPermissionSchemaSpaceId(permissionSchemaSpaceId);
        return t;
    }

    @Nested
    class Load {

        @Test
        void load_collectsPermissionSchemaSpaceIdsAndQueriesRepositoryByThem() {
            // Contract: the per-twin permission schema space drives which space-role-users to load.
            var space1 = UUID.randomUUID();
            var space2 = UUID.randomUUID();
            var t1 = twin(UUID.randomUUID(), space1);
            var t2 = twin(UUID.randomUUID(), space2);
            var kit = new Kit<>(java.util.Arrays.asList(t1, t2), TwinEntity::getId);

            when(spaceRoleUserRepository.findByTwinIdIn(any(Set.class)))
                    .thenReturn(List.of());

            storage().load(kit);

            // Repository must be queried with the collected space set (de-duplicated).
            var captor = org.mockito.ArgumentCaptor.forClass(Set.class);
            verify(spaceRoleUserRepository).findByTwinIdIn(captor.capture());
            assertEquals(Set.of(space1, space2), captor.getValue());
        }

        @Test
        void load_twinWithoutMatchingRows_isInitialisedWithEmptyKit() {
            var space = UUID.randomUUID();
            var t = twin(UUID.randomUUID(), space);
            var kit = new Kit<>(List.of(t), TwinEntity::getId);

            when(spaceRoleUserRepository.findByTwinIdIn(any(Set.class)))
                    .thenReturn(List.of());

            storage().load(kit);

            assertEquals(KitGrouped.EMPTY, t.getTwinFieldSpaceUserKit());
        }
    }

    private TwinFieldStorageSpaceRoleUser storage() {
        return new TwinFieldStorageSpaceRoleUser(spaceRoleUserRepository);
    }

    @Nested
    class StrictValues {

        @Test
        void hasStrictValues_isAlwaysFalse() {
            assertFalse(storage().hasStrictValues(UUID.randomUUID()));
        }
    }

    @Nested
    class LoadState {

        @Test
        void isLoaded_nullKit_isFalse() {
            assertFalse(storage().isLoaded(twin(UUID.randomUUID(), UUID.randomUUID())));
        }

        @Test
        void isLoaded_afterInitEmpty_isTrue() {
            var s = storage();
            var t = twin(UUID.randomUUID(), UUID.randomUUID());

            s.initEmpty(t);

            assertTrue(s.isLoaded(t));
        }
    }

    @Nested
    class NoopOperations {

        @Test
        void findUsedFields_returnsEmptyList() {
            assertEquals(List.of(), storage().findUsedFields(UUID.randomUUID(), Set.of()));
        }

        @Test
        void replaceTwinClassFieldForTwinsOfClass_isNoop_doesNotCallRepository() {
            storage().replaceTwinClassFieldForTwinsOfClass(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID());

            verifyNoInteractions(spaceRoleUserRepository);
        }

        @Test
        void deleteTwinFieldsForTwins_isNoop_doesNotCallRepository() {
            storage().deleteTwinFieldsForTwins(Map.of(UUID.randomUUID(), Set.of()));

            verifyNoInteractions(spaceRoleUserRepository);
        }
    }

    @Nested
    class EqualsAndMerge {

        @Test
        void equals_twoInstancesOfSameClass_isTrue() {
            assertEquals(storage(), storage());
        }

        @Test
        void equals_differentStorageClass_isFalse() {
            assertNotEquals(storage(), new TwinFieldStorageSpirit());
        }
    }
}
