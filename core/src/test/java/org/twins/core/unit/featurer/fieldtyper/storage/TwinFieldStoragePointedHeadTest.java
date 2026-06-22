package org.twins.core.unit.featurer.fieldtyper.storage;

import org.cambium.common.exception.ServiceException;
import org.cambium.common.kit.Kit;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.dao.twin.TwinEntity;
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

    private TwinEntity twin(UUID id) {
        var t = new TwinEntity();
        t.setId(id);
        return t;
    }

    @Nested
    class Load {

        @Test
        void load_delegatesHeadLoadingToTwinServiceThenLoadsHeadFields() throws ServiceException {
            var storage = new TwinFieldStoragePointedHead(twinService);
            var t1 = twin(UUID.randomUUID());
            var head1 = twin(UUID.randomUUID());
            t1.setHeadTwinId(head1.getId());
            t1.setHeadTwin(head1);
            var kit = new Kit<>(List.of(t1), TwinEntity::getId);

            storage.load(kit);

            // PointedHead contract: load head for every twin, then load fields of the collected head set.
            verify(twinService).loadHeadForTwin(kit.getList());
            var headsCaptor = org.mockito.ArgumentCaptor.forClass(java.util.Collection.class);
            verify(twinService).loadFieldsValues(headsCaptor.capture());
            assertTrue(headsCaptor.getValue().contains(head1));
        }

        @Test
        void load_twinWithoutHead_doesNotAddAnythingToHeadSet() throws ServiceException {
            var storage = new TwinFieldStoragePointedHead(twinService);
            var t1 = twin(UUID.randomUUID());
            var kit = new Kit<>(List.of(t1), TwinEntity::getId);

            storage.load(kit);

            verify(twinService).loadHeadForTwin(kit.getList());
            // Still called (with whatever head set accumulated — here nothing), but must not throw.
            verify(twinService).loadFieldsValues((java.util.Collection<TwinEntity>) any());
        }
    }

    @Nested
    class LoadState {

        @Test
        void isLoaded_twinWithNoHeadTwinId_isTrue() {
            // No head to load -> trivially loaded.
            var storage = new TwinFieldStoragePointedHead(twinService);
            var t = twin(UUID.randomUUID());

            assertTrue(storage.isLoaded(t));
        }

        @Test
        void isLoaded_headLoadedWithFieldValuesKit_isTrue() {
            var storage = new TwinFieldStoragePointedHead(twinService);
            var t = twin(UUID.randomUUID());
            var head = twin(UUID.randomUUID());
            t.setHeadTwinId(head.getId());
            t.setHeadTwin(head);
            head.setFieldValuesKit(Kit.EMPTY);

            assertTrue(storage.isLoaded(t));
        }

        @Test
        void isLoaded_headNotYetLoaded_isFalse() {
            var storage = new TwinFieldStoragePointedHead(twinService);
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
                    new TwinFieldStoragePointedHead(twinService),
                    new TwinFieldStoragePointedHead(twinService));
        }

        @Test
        void equals_differentStorageClass_isFalse() {
            assertNotEquals(new TwinFieldStoragePointedHead(twinService), new TwinFieldStorageSpirit());
        }
    }
}
