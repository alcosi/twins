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
import org.twins.core.dao.twin.TwinFieldSimpleRepository;
import org.twins.core.featurer.fieldtyper.storage.TwinFieldStorageCalcChildrenOfClassCount;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TwinFieldStorageCalcChildrenOfClassCountTest extends BaseUnitTest {

    @Mock
    private TwinFieldSimpleRepository twinFieldSimpleRepository;

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

    @Nested
    class LoadByExtendsHierarchy {

        @Test
        void load_lqueryConstructor_countsByExtendsHierarchy() throws ServiceException {
            var lquery = "classA.*";
            var storage = new TwinFieldStorageCalcChildrenOfClassCount(
                    twinFieldSimpleRepository, fieldId, lquery);
            var head1 = twin(UUID.randomUUID());
            var kit = new Kit<>(java.util.List.of(head1), TwinEntity::getId);

            when(twinFieldSimpleRepository.countChildrenTwinsByExtendsHierarchy(
                    eq(kit.getIdSet()), eq(lquery)))
                    .thenReturn(List.of(new TwinFieldCalcProjection(head1.getId(), new BigDecimal("3"))));

            storage.load(kit);

            verify(twinFieldSimpleRepository).countChildrenTwinsByExtendsHierarchy(
                    eq(kit.getIdSet()), eq(lquery));
            verify(twinFieldSimpleRepository, never()).countChildrenTwinsOfTwinClassIdIn(anyCollection(), anyCollection());
            assertEquals(new BigDecimal("3"), head1.getTwinFieldCalculated().get(fieldId));
        }

        @Test
        void load_twinWithoutProjection_defaultsToZero() throws ServiceException {
            var storage = new TwinFieldStorageCalcChildrenOfClassCount(
                    twinFieldSimpleRepository, fieldId, "x.*");
            var head1 = twin(UUID.randomUUID());
            var head2 = twin(UUID.randomUUID());
            var kit = new Kit<>(java.util.Arrays.asList(head1, head2), TwinEntity::getId);

            when(twinFieldSimpleRepository.countChildrenTwinsByExtendsHierarchy(anyCollection(), anyString()))
                    .thenReturn(List.of(new TwinFieldCalcProjection(head1.getId(), new BigDecimal("1"))));

            storage.load(kit);

            assertEquals(new BigDecimal("1"), head1.getTwinFieldCalculated().get(fieldId));
            assertEquals(BigDecimal.ZERO, head2.getTwinFieldCalculated().get(fieldId));
        }
    }

    @Nested
    class LoadByClassIds {

        @Test
        void load_classIdsConstructor_countsByTwinClassIdIn() throws ServiceException {
            var classIds = Set.of(UUID.randomUUID(), UUID.randomUUID());
            var storage = new TwinFieldStorageCalcChildrenOfClassCount(
                    twinFieldSimpleRepository, fieldId, classIds);
            var head1 = twin(UUID.randomUUID());
            var kit = new Kit<>(java.util.List.of(head1), TwinEntity::getId);

            when(twinFieldSimpleRepository.countChildrenTwinsOfTwinClassIdIn(
                    eq(kit.getIdSet()), eq(classIds)))
                    .thenReturn(List.of(new TwinFieldCalcProjection(head1.getId(), new BigDecimal("7"))));

            storage.load(kit);

            verify(twinFieldSimpleRepository).countChildrenTwinsOfTwinClassIdIn(
                    eq(kit.getIdSet()), eq(classIds));
            verify(twinFieldSimpleRepository, never()).countChildrenTwinsByExtendsHierarchy(anyCollection(), anyString());
            assertEquals(new BigDecimal("7"), head1.getTwinFieldCalculated().get(fieldId));
        }
    }

    @Nested
    class EqualsAndMerge {

        @Test
        void equals_sameLquery_isTrue() throws ServiceException {
            assertEquals(
                    new TwinFieldStorageCalcChildrenOfClassCount(twinFieldSimpleRepository, fieldId, "a.*"),
                    new TwinFieldStorageCalcChildrenOfClassCount(twinFieldSimpleRepository, fieldId, "a.*"));
        }

        @Test
        void equals_differentLquery_isFalse() throws ServiceException {
            assertNotEquals(
                    new TwinFieldStorageCalcChildrenOfClassCount(twinFieldSimpleRepository, fieldId, "a.*"),
                    new TwinFieldStorageCalcChildrenOfClassCount(twinFieldSimpleRepository, fieldId, "b.*"));
        }

        @Test
        void equals_lqueryVsClassIds_isFalse() throws ServiceException {
            // Different modes (extendsHierarchy vs classIds) must NOT merge — they call different queries.
            assertNotEquals(
                    new TwinFieldStorageCalcChildrenOfClassCount(twinFieldSimpleRepository, fieldId, "a.*"),
                    new TwinFieldStorageCalcChildrenOfClassCount(twinFieldSimpleRepository, fieldId, Set.of(UUID.randomUUID())));
        }

        @Test
        void equals_sameClassIds_isTrue() throws ServiceException {
            var ids = Set.of(UUID.randomUUID());
            assertEquals(
                    new TwinFieldStorageCalcChildrenOfClassCount(twinFieldSimpleRepository, fieldId, ids),
                    new TwinFieldStorageCalcChildrenOfClassCount(twinFieldSimpleRepository, fieldId, ids));
        }
    }
}
