package org.twins.core.unit.featurer.fieldtyper.storage;

import org.cambium.common.exception.ServiceException;
import org.cambium.common.kit.Kit;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.featurer.fieldtyper.storage.TwinFieldStorage;
import org.twins.core.featurer.fieldtyper.storage.TwinFieldStoragePointedHead;
import org.twins.core.featurer.fieldtyper.storage.TwinFieldStorageSpirit;
import org.twins.core.service.twin.TwinService;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TwinFieldStoragePointedHeadTest extends BaseUnitTest {

    @Mock
    private TwinService twinService;

    @Mock
    private TwinFieldStorage headTwinFieldStorage;

    private TwinEntity twin(UUID id) {
        var t = new TwinEntity();
        t.setId(id);
        return t;
    }

    @Nested
    class Load {

        @Test
        void load_loadsHeadThenDelegatesHeadFieldLoadingToHeadStorage() throws ServiceException {
            var storage = new TwinFieldStoragePointedHead(twinService, headTwinFieldStorage);
            var t1 = twin(UUID.randomUUID());
            var head1 = twin(UUID.randomUUID());
            t1.setHeadTwinId(head1.getId());
            t1.setHeadTwin(head1);
            var kit = new Kit<>(List.of(t1), TwinEntity::getId);

            storage.load(kit);

            // PointedHead contract: load head for every twin, then delegate head-field loading to the
            // nested head storage over the collected head set.
            verify(twinService).loadHead(kit.getList());
            var headsCaptor = ArgumentCaptor.forClass(Kit.class);
            verify(headTwinFieldStorage).load(headsCaptor.capture());
            assertTrue(headsCaptor.getValue().getCollection().contains(head1));
        }

        @Test
        void load_twinWithoutHead_delegatesEmptyHeadSet() throws ServiceException {
            var storage = new TwinFieldStoragePointedHead(twinService, headTwinFieldStorage);
            var t1 = twin(UUID.randomUUID());
            var kit = new Kit<>(List.of(t1), TwinEntity::getId);

            storage.load(kit);

            verify(twinService).loadHead(kit.getList());
            var headsCaptor = ArgumentCaptor.forClass(Kit.class);
            verify(headTwinFieldStorage).load(headsCaptor.capture());
            assertTrue(headsCaptor.getValue().getCollection().isEmpty());
        }
    }

    @Nested
    class LoadState {

        @Test
        void isLoaded_twinWithNoHeadTwinId_isTrue() {
            // No head to load -> trivially loaded.
            var storage = new TwinFieldStoragePointedHead(twinService, headTwinFieldStorage);
            var t = twin(UUID.randomUUID());

            assertTrue(storage.isLoaded(t));
        }

        @Test
        void isLoaded_headLoadedInHeadStorage_isTrue() {
            var storage = new TwinFieldStoragePointedHead(twinService, headTwinFieldStorage);
            var t = twin(UUID.randomUUID());
            var head = twin(UUID.randomUUID());
            t.setHeadTwinId(head.getId());
            t.setHeadTwin(head);
            when(headTwinFieldStorage.isLoaded(head)).thenReturn(true);

            assertTrue(storage.isLoaded(t));
        }

        @Test
        void isLoaded_headNotYetLoaded_isFalse() {
            var storage = new TwinFieldStoragePointedHead(twinService, headTwinFieldStorage);
            var t = twin(UUID.randomUUID());
            var head = twin(UUID.randomUUID());
            t.setHeadTwinId(head.getId());
            t.setHeadTwin(head);

            assertFalse(storage.isLoaded(t));
        }
    }

    @Nested
    class EqualsAndMerge {

        @Test
        void equals_twoInstancesOfSameClass_isTrue() {
            assertEquals(
                    new TwinFieldStoragePointedHead(twinService, headTwinFieldStorage),
                    new TwinFieldStoragePointedHead(twinService, headTwinFieldStorage));
        }

        @Test
        void equals_differentStorageClass_isFalse() {
            assertNotEquals(new TwinFieldStoragePointedHead(twinService, headTwinFieldStorage), new TwinFieldStorageSpirit());
        }
    }
}
