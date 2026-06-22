package org.twins.core.unit.featurer.fieldtyper.storage;

import org.cambium.common.exception.ServiceException;
import org.cambium.common.kit.Kit;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.featurer.fieldtyper.storage.TwinFieldStorageLink;
import org.twins.core.featurer.fieldtyper.storage.TwinFieldStorageSpirit;
import org.twins.core.service.link.TwinLinkService;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TwinFieldStorageLinkTest extends BaseUnitTest {

    @Mock
    private TwinLinkService twinLinkService;

    private TwinEntity twin() {
        var t = new TwinEntity();
        t.setId(UUID.randomUUID());
        return t;
    }

    @Nested
    class Load {

        @Test
        void load_delegatesToTwinLinkServiceWithTwinCollection() throws ServiceException {
            var storage = new TwinFieldStorageLink(twinLinkService);
            var t1 = twin();
            var t2 = twin();
            var kit = new Kit<>(java.util.Arrays.asList(t1, t2), TwinEntity::getId);

            storage.load(kit);

            // load() must hand the twin collection off to TwinLinkService — that is its only job.
            verify(twinLinkService).loadTwinLinks(kit.getCollection());
        }
    }

    @Nested
    class StrictValues {

        @Test
        void hasStrictValues_isAlwaysFalse() {
            assertFalse(new TwinFieldStorageLink(twinLinkService).hasStrictValues(UUID.randomUUID()));
        }
    }

    @Nested
    class LoadState {

        @Test
        void isLoaded_nullTwinLinks_isFalse() {
            var storage = new TwinFieldStorageLink(twinLinkService);

            assertFalse(storage.isLoaded(twin()));
        }

        @Test
        void isLoaded_afterInitEmpty_isTrue() {
            // initEmpty sets the canonical EMPTY result singleton.
            var storage = new TwinFieldStorageLink(twinLinkService);
            var t = twin();

            storage.initEmpty(t);

            assertSame(TwinLinkService.FindTwinLinksResult.EMPTY, t.getTwinLinks());
            assertTrue(storage.isLoaded(t));
        }
    }

    @Nested
    class NoopOperations {

        @Test
        void findUsedFields_returnsEmptyList() {
            assertEquals(List.of(),
                    new TwinFieldStorageLink(twinLinkService).findUsedFields(UUID.randomUUID(), Set.of()));
        }

        @Test
        void replaceTwinClassFieldForTwinsOfClass_isNoop_doesNotCallService() {
            new TwinFieldStorageLink(twinLinkService)
                    .replaceTwinClassFieldForTwinsOfClass(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID());

            verifyNoInteractions(twinLinkService);
        }

        @Test
        void deleteTwinFieldsForTwins_isNoop_doesNotCallService() {
            new TwinFieldStorageLink(twinLinkService)
                    .deleteTwinFieldsForTwins(Map.of(UUID.randomUUID(), Set.of()));

            verifyNoInteractions(twinLinkService);
        }
    }

    @Nested
    class EqualsAndMerge {

        @Test
        void equals_twoInstancesOfSameClass_isTrue() {
            assertEquals(
                    new TwinFieldStorageLink(twinLinkService),
                    new TwinFieldStorageLink(twinLinkService));
        }

        @Test
        void equals_differentStorageClass_isFalse() {
            assertNotEquals(new TwinFieldStorageLink(twinLinkService), new TwinFieldStorageSpirit());
        }
    }
}
