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
import org.twins.core.featurer.fieldtyper.storage.TwinFieldStorageCalcSumOfMultiplicationsByHead;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TwinFieldStorageCalcSumOfMultiplicationsByHeadTest extends BaseUnitTest {

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

    private TwinFieldStorageCalcSumOfMultiplicationsByHead newStorage(boolean exclude) {
        return new TwinFieldStorageCalcSumOfMultiplicationsByHead(
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
        void load_delegatesToMultiplicationsByHeadRepository_andPacksResults() throws ServiceException {
            var storage = newStorage(true);
            var head1 = twin(UUID.randomUUID());
            var head2 = twin(UUID.randomUUID());
            var kit = new Kit<>(java.util.Arrays.asList(head1, head2), TwinEntity::getId);

            when(twinFieldDecimalRepository.sumChildrenTwinFieldValuesOfMultiplicationsByHead(
                    eq(kit.getIdSet()), eq(childStatusIds), eq(childOfClassIds),
                    eq(firstFieldId), eq(secondFieldId), eq(true)))
                    .thenReturn(List.of(
                            new TwinFieldCalcProjection(head1.getId(), new BigDecimal("6")),
                            new TwinFieldCalcProjection(head2.getId(), new BigDecimal("20"))));

            storage.load(kit);

            verify(twinFieldDecimalRepository).sumChildrenTwinFieldValuesOfMultiplicationsByHead(
                    eq(kit.getIdSet()), eq(childStatusIds), eq(childOfClassIds),
                    eq(firstFieldId), eq(secondFieldId), eq(true));
            verify(twinFieldDecimalRepository, never()).sumChildrenTwinFieldValuesOfDivisionsByHead(
                    any(), any(), any(), any(), any(), anyBoolean(), anyBoolean());
            assertEquals(new BigDecimal("6"), head1.getTwinFieldCalculated().get(fieldId));
            assertEquals(new BigDecimal("20"), head2.getTwinFieldCalculated().get(fieldId));
        }

        @Test
        void load_twinWithoutProjection_defaultsToZero() throws ServiceException {
            var storage = newStorage(false);
            var head1 = twin(UUID.randomUUID());
            var head2 = twin(UUID.randomUUID());
            var kit = new Kit<>(java.util.Arrays.asList(head1, head2), TwinEntity::getId);

            when(twinFieldDecimalRepository.sumChildrenTwinFieldValuesOfMultiplicationsByHead(
                    any(), any(), any(), any(), any(), anyBoolean()))
                    .thenReturn(List.of(new TwinFieldCalcProjection(head1.getId(), new BigDecimal("42"))));

            storage.load(kit);

            assertEquals(new BigDecimal("42"), head1.getTwinFieldCalculated().get(fieldId));
            assertEquals(BigDecimal.ZERO, head2.getTwinFieldCalculated().get(fieldId));
        }

        @Test
        void load_emptyProjections_yieldsZero() throws ServiceException {
            var storage = newStorage(false);
            var head1 = twin(UUID.randomUUID());
            var kit = new Kit<>(java.util.List.of(head1), TwinEntity::getId);

            when(twinFieldDecimalRepository.sumChildrenTwinFieldValuesOfMultiplicationsByHead(
                    any(), any(), any(), any(), any(), anyBoolean()))
                    .thenReturn(List.of());

            storage.load(kit);

            assertEquals(BigDecimal.ZERO, head1.getTwinFieldCalculated().get(fieldId));
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
        void equals_differentSecondField_isFalse() throws ServiceException {
            var a = newStorage(true);
            var b = new TwinFieldStorageCalcSumOfMultiplicationsByHead(
                    fieldId, twinFieldDecimalRepository, firstFieldId, UUID.randomUUID(),
                    childStatusIds, childOfClassIds, true);
            assertNotEquals(a, b);
        }
    }
}
