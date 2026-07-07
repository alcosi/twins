package org.twins.core.unit.featurer.fieldtyper.storage;

import org.cambium.common.kit.Kit;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.dao.attachment.TwinAttachmentRepository;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.featurer.fieldtyper.storage.TwinFieldStorageAttachment;
import org.twins.core.featurer.fieldtyper.storage.TwinFieldStorageSpirit;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TwinFieldStorageAttachmentTest extends BaseUnitTest {

    @Mock
    private TwinAttachmentRepository twinAttachmentRepository;

    private TwinEntity twin() {
        var t = new TwinEntity();
        t.setId(UUID.randomUUID());
        return t;
    }

    @Nested
    class Load {

        @Test
        void load_isNoop_doesNotMutateTwins() throws org.cambium.common.exception.ServiceException {
            // Attachments are not browsable as fields; load must be a no-op.
            var storage = new TwinFieldStorageAttachment(twinAttachmentRepository);
            var t = twin();
            var kit = new Kit<>(List.of(t), TwinEntity::getId);

            storage.load(kit);

            assertNull(t.getTwinFieldSimpleKit());
            assertNull(t.getTwinFieldBooleanKit());
            verifyNoInteractions(twinAttachmentRepository);
        }
    }

    @Nested
    class StrictValues {

        @Test
        void hasStrictValues_delegatesToRepositoryExistsCheck() {
            var f = UUID.randomUUID();
            when(twinAttachmentRepository.existsByTwinClassFieldId(f)).thenReturn(true);

            assertTrue(new TwinFieldStorageAttachment(twinAttachmentRepository).hasStrictValues(f));
        }

        @Test
        void hasStrictValues_repoSaysNo_returnsFalse() {
            when(twinAttachmentRepository.existsByTwinClassFieldId(any(UUID.class))).thenReturn(false);

            assertFalse(new TwinFieldStorageAttachment(twinAttachmentRepository).hasStrictValues(UUID.randomUUID()));
        }
    }

    @Nested
    class LoadState {

        @Test
        void isLoaded_isAlwaysTrue_attachmentHasNoPerTwinState() {
            var storage = new TwinFieldStorageAttachment(twinAttachmentRepository);
            var t = new TwinEntity();

            assertTrue(storage.isLoaded(t));
        }

        @Test
        void initEmpty_isNoop_doesNotMutateTwin() {
            var storage = new TwinFieldStorageAttachment(twinAttachmentRepository);
            var t = new TwinEntity();

            storage.initEmpty(t);

            assertNull(t.getTwinFieldSimpleKit());
        }
    }

    @Nested
    class DelegatingOperations {

        @Test
        void findUsedFields_returnsEmptyList() {
            assertEquals(List.of(),
                    new TwinFieldStorageAttachment(twinAttachmentRepository).findUsedFields(UUID.randomUUID(), Set.of()));
        }

        @Test
        void replaceTwinClassFieldForTwinsOfClass_delegatesToRepository() {
            var storage = new TwinFieldStorageAttachment(twinAttachmentRepository);
            var classId = UUID.randomUUID();
            var from = UUID.randomUUID();
            var to = UUID.randomUUID();

            storage.replaceTwinClassFieldForTwinsOfClass(classId, from, to);

            verify(twinAttachmentRepository).replaceTwinClassFieldForTwinsOfClass(classId, from, to);
        }

        @Test
        void deleteTwinFieldsForTwins_isNoop_doesNotCallRepository() {
            new TwinFieldStorageAttachment(twinAttachmentRepository)
                    .deleteTwinFieldsForTwins(Map.of(UUID.randomUUID(), Set.of()));

            verifyNoInteractions(twinAttachmentRepository);
        }
    }

    @Nested
    class EqualsAndMerge {

        @Test
        void equals_twoInstancesOfSameClass_isTrue() {
            assertEquals(
                    new TwinFieldStorageAttachment(twinAttachmentRepository),
                    new TwinFieldStorageAttachment(twinAttachmentRepository));
        }

        @Test
        void equals_differentStorageClass_isFalse() {
            assertNotEquals(
                    new TwinFieldStorageAttachment(twinAttachmentRepository),
                    new TwinFieldStorageSpirit());
        }
    }
}
