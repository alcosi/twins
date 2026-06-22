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
import org.twins.core.dao.twin.TwinFieldDecimalRepository;
import org.twins.core.featurer.fieldtyper.storage.TwinFieldStorageCalcSumOfSubtractionsByHead;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TwinFieldStorageCalcSumOfSubtractionsByHeadTest extends BaseUnitTest {

    @Mock
    private TwinFieldDecimalRepository twinFieldDecimalRepository;

    private UUID fieldId;
    private UUID firstFieldId;
    private UUID secondFieldId;
    private Set<UUID> childStatusIds;
    private Set<UUID> childOfClassIds;

    @BeforeEach
    void setUp() {
        fieldId = UUID.randomUUID();
        firstFieldId = UUID.randomUUID();
        secondFieldId = UUID.randomUUID();
        childStatusIds = Set.of(UUID.randomUUID());
        childOfClassIds = Set.of(UUID.randomUUID());
    }

    private TwinFieldStorageCalcSumOfSubtractionsByHead newStorage(boolean exclude) {
        return new TwinFieldStorageCalcSumOfSubtractionsByHead(
                fieldId, twinFieldDecimalRepository, firstFieldId, secondFieldId,
                childStatusIds, childOfClassIds, exclude);
    }

    private TwinEntity twin(UUID id) {
        var t = new TwinEntity();
        t.setId(id);
        return t;
    }

    @Nested
    class Load {

        @Test
        void load_delegatesToSubtractionsByHeadRepository_andPacksResults() throws ServiceException {
            var storage = newStorage(true);
            var head1 = twin(UUID.randomUUID());
            var head2 = twin(UUID.randomUUID());
            var kit = new Kit<>(java.util.Arrays.asList(head1, head2), TwinEntity::getId);

            when(twinFieldDecimalRepository.sumChildrenTwinFieldValuesOfSubtractionsByHead(
                    eq(kit.getIdSet()), eq(childStatusIds), eq(childOfClassIds),
                    eq(firstFieldId), eq(secondFieldId), eq(true)))
                    .thenReturn(List.of(
                            new TwinFieldCalcProjection(head1.getId(), new BigDecimal("3")),
                            new TwinFieldCalcProjection(head2.getId(), new BigDecimal("-1"))));

            storage.load(kit);

            verify(twinFieldDecimalRepository).sumChildrenTwinFieldValuesOfSubtractionsByHead(
                    eq(kit.getIdSet()), eq(childStatusIds), eq(childOfClassIds),
                    eq(firstFieldId), eq(secondFieldId), eq(true));
            verify(twinFieldDecimalRepository, never()).sumChildrenTwinFieldValuesOfMultiplicationsByHead(
                    any(), any(), any(), any(), any(), anyBoolean());
            assertEquals(new BigDecimal("3"), head1.getTwinFieldCalculated().get(fieldId));
            assertEquals(new BigDecimal("-1"), head2.getTwinFieldCalculated().get(fieldId));
        }

        @Test
        void load_twinWithoutProjection_defaultsToZero() throws ServiceException {
            var storage = newStorage(false);
            var head1 = twin(UUID.randomUUID());
            var head2 = twin(UUID.randomUUID());
            var kit = new Kit<>(java.util.Arrays.asList(head1, head2), TwinEntity::getId);

            when(twinFieldDecimalRepository.sumChildrenTwinFieldValuesOfSubtractionsByHead(
                    any(), any(), any(), any(), any(), anyBoolean()))
                    .thenReturn(List.of(new TwinFieldCalcProjection(head1.getId(), new BigDecimal("7"))));

            storage.load(kit);

            assertEquals(new BigDecimal("7"), head1.getTwinFieldCalculated().get(fieldId));
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
    }
}
