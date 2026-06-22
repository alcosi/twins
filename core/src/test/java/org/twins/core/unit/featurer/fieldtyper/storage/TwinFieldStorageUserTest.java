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
import org.twins.core.dao.twin.TwinFieldUserEntity;
import org.twins.core.dao.twin.TwinFieldUserRepository;
import org.twins.core.featurer.fieldtyper.storage.TwinFieldStorageUser;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TwinFieldStorageUserTest extends BaseUnitTest {

    @Mock
    private TwinFieldUserRepository twinFieldUserRepository;

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

    private TwinFieldUserEntity field(UUID twinId, UUID classFieldId) {
        var e = new TwinFieldUserEntity();
        e.setId(UUID.randomUUID());
        e.setTwinId(twinId);
        e.setTwinClassFieldId(classFieldId);
        return e;
    }

    @Nested
    class Load {

        @Test
        void load_groupsByTwinIdAndPopulatesPerTwinUserKit() {
            var storage = new TwinFieldStorageUser(twinFieldUserRepository);
            var t1 = twin(UUID.randomUUID());
            var kit = new Kit<>(List.of(t1), TwinEntity::getId);

            when(twinFieldUserRepository.findByTwinIdIn(kit.getIdSet()))
                    .thenReturn(List.of(field(t1.getId(), fieldId)));

            storage.load(kit);

            assertNotNull(t1.getTwinFieldUserKit());
            assertTrue(t1.getTwinFieldUserKit().containsGroupedKey(fieldId));
        }

        @Test
        void load_absentTwin_isInitialisedWithEmptyKit() {
            var storage = new TwinFieldStorageUser(twinFieldUserRepository);
            var t1 = twin(UUID.randomUUID());
            var kit = new Kit<>(List.of(t1), TwinEntity::getId);

            when(twinFieldUserRepository.findByTwinIdIn(kit.getIdSet()))
                    .thenReturn(List.of());

            storage.load(kit);

            assertEquals(KitGrouped.EMPTY, t1.getTwinFieldUserKit());
        }
    }

    @Nested
    class StrictValues {

        @Test
        void hasStrictValues_delegatesToRepository() {
            var f = UUID.randomUUID();
            when(twinFieldUserRepository.existsByTwinClassFieldId(f)).thenReturn(true);

            assertTrue(new TwinFieldStorageUser(twinFieldUserRepository).hasStrictValues(f));
        }
    }

    @Nested
    class LoadState {

        @Test
        void isLoaded_reflectsUserKitPresence() {
            var storage = new TwinFieldStorageUser(twinFieldUserRepository);
            var t = twin(UUID.randomUUID());

            assertFalse(storage.isLoaded(t));

            storage.initEmpty(t);

            assertTrue(storage.isLoaded(t));
        }
    }

    @Nested
    class DelegatingOperations {

        @Test
        void findUsedFields_returnsEmptyList_userStorageHasNoFieldIndex() {
            // User storage does not index per twin-class field usage (no findUsedFields delegate).
            assertEquals(List.of(),
                    new TwinFieldStorageUser(twinFieldUserRepository).findUsedFields(UUID.randomUUID(), Set.of()));
        }

        @Test
        void replaceTwinClassFieldForTwinsOfClass_isNoop_doesNotCallRepository() {
            // User storage documents "nothing to replace" — verify no repo interaction.
            var storage = new TwinFieldStorageUser(twinFieldUserRepository);

            storage.replaceTwinClassFieldForTwinsOfClass(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID());

            verifyNoInteractions(twinFieldUserRepository);
        }

        @Test
        void deleteTwinFieldsForTwins_delegatesPerEntry() {
            var storage = new TwinFieldStorageUser(twinFieldUserRepository);
            var twinId = UUID.randomUUID();
            var fields = Set.<UUID>of(UUID.randomUUID());

            storage.deleteTwinFieldsForTwins(Map.of(twinId, fields));

            verify(twinFieldUserRepository).deleteByTwinIdAndTwinClassFieldIdIn(twinId, fields);
        }
    }

    @Nested
    class EqualsAndMerge {

        @Test
        void equals_twoInstancesOfSameClass_isTrue() {
            assertEquals(
                    new TwinFieldStorageUser(twinFieldUserRepository),
                    new TwinFieldStorageUser(twinFieldUserRepository));
        }

        @Test
        void equals_differentStorageClass_isFalse() {
            assertNotEquals(new TwinFieldStorageUser(twinFieldUserRepository), new TwinFieldStorageSpirit());
        }
    }
}
