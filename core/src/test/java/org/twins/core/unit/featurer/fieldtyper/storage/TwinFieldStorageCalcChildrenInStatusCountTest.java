package org.twins.core.unit.featurer.fieldtyper.storage;

import org.cambium.common.exception.ServiceException;
import org.cambium.common.kit.Kit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinFieldCalcProjection;
import org.twins.core.dao.twin.TwinRepository;
import org.twins.core.featurer.fieldtyper.storage.TwinFieldStorageCalcChildrenInStatusCount;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TwinFieldStorageCalcChildrenInStatusCountTest extends BaseUnitTest {

    @Mock
    private TwinRepository twinRepository;

    private UUID fieldId;
    private Set<UUID> statusIds;

    @BeforeEach
    void setUp() {
        fieldId = UUID.randomUUID();
        statusIds = Set.of(UUID.randomUUID());
    }

    private TwinFieldStorageCalcChildrenInStatusCount newStorage(boolean exclude) {
        return new TwinFieldStorageCalcChildrenInStatusCount(twinRepository, fieldId, statusIds, exclude);
    }

    private TwinEntity twin(UUID id) {
        var t = new TwinEntity();
        t.setId(id);
        return t;
    }

    @Nested
    class Load {

        @Test
        void load_excludeFalse_countsChildrenWithStatusIn() throws ServiceException {
            var storage = newStorage(false);
            var head1 = twin(UUID.randomUUID());
            var kit = new Kit<>(java.util.List.of(head1), TwinEntity::getId);

            when(twinRepository.countChildrenTwinsWithStatusIn(
                    eq(kit.getIdSet()), eq(statusIds)))
                    .thenReturn(List.of(new TwinFieldCalcProjection(head1.getId(), new BigDecimal("5"))));

            storage.load(kit);

            verify(twinRepository).countChildrenTwinsWithStatusIn(
                    eq(kit.getIdSet()), eq(statusIds));
            verify(twinRepository, never()).countChildrenTwinsWithStatusNotIn(anyCollection(), anyCollection());
            assertEquals(new BigDecimal("5"), head1.getTwinFieldCalculated().get(fieldId));
        }

        @Test
        void load_excludeTrue_countsChildrenWithStatusNotIn() throws ServiceException {
            var storage = newStorage(true);
            var head1 = twin(UUID.randomUUID());
            var kit = new Kit<>(java.util.List.of(head1), TwinEntity::getId);

            when(twinRepository.countChildrenTwinsWithStatusNotIn(
                    eq(kit.getIdSet()), eq(statusIds)))
                    .thenReturn(List.of(new TwinFieldCalcProjection(head1.getId(), new BigDecimal("9"))));

            storage.load(kit);

            // exclude=true must invert the filter -> StatusNotIn
            verify(twinRepository).countChildrenTwinsWithStatusNotIn(
                    eq(kit.getIdSet()), eq(statusIds));
            verify(twinRepository, never()).countChildrenTwinsWithStatusIn(anyCollection(), anyCollection());
            assertEquals(new BigDecimal("9"), head1.getTwinFieldCalculated().get(fieldId));
        }

        @Test
        void load_twinWithoutProjection_defaultsToZero() throws ServiceException {
            var storage = newStorage(false);
            var head1 = twin(UUID.randomUUID());
            var head2 = twin(UUID.randomUUID());
            var kit = new Kit<>(java.util.Arrays.asList(head1, head2), TwinEntity::getId);

            when(twinRepository.countChildrenTwinsWithStatusIn(anyCollection(), anyCollection()))
                    .thenReturn(List.of(new TwinFieldCalcProjection(head1.getId(), new BigDecimal("2"))));

            storage.load(kit);

            assertEquals(new BigDecimal("2"), head1.getTwinFieldCalculated().get(fieldId));
            assertEquals(BigDecimal.ZERO, head2.getTwinFieldCalculated().get(fieldId));
        }
    }

    @Nested
    class EqualsAndMerge {

        @Test
        void equals_sameConfigDifferentInstance_isTrue() throws ServiceException {
            assertEquals(newStorage(true), newStorage(true));
        }

        @Test
        void equals_differentExclude_isFalse() throws ServiceException {
            assertNotEquals(newStorage(true), newStorage(false));
        }

        @Test
        void equals_differentStatusSet_isFalse() throws ServiceException {
            var a = newStorage(true);
            var b = new TwinFieldStorageCalcChildrenInStatusCount(
                    twinRepository, fieldId, Set.of(UUID.randomUUID()), true);
            assertNotEquals(a, b);
        }
    }
}
