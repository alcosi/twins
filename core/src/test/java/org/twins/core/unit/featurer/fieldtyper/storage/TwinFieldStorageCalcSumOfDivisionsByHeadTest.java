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
import org.twins.core.featurer.fieldtyper.storage.TwinFieldStorageCalcSumOfDivisionsByHead;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.mockito.Mockito.*;

class TwinFieldStorageCalcSumOfDivisionsByHeadTest extends BaseUnitTest {

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

    private TwinFieldStorageCalcSumOfDivisionsByHead newStorage(boolean exclude, boolean divisionByZeroIgnore) {
        return new TwinFieldStorageCalcSumOfDivisionsByHead(
                fieldId, twinFieldDecimalRepository, firstFieldId, secondFieldId,
                childStatusIds, childOfClassIds, exclude, divisionByZeroIgnore);
    }

    private TwinEntity twin(UUID id) {
        var t = new TwinEntity();
        t.setId(id);
        return t;
    }

    @Nested
    class Load {

        @Test
        void load_delegatesToDivisionsByHeadRepository_andPacksResults() throws ServiceException {
            var storage = newStorage(true, false);
            var head1 = twin(UUID.randomUUID());
            var head2 = twin(UUID.randomUUID());
            var kit = new Kit<>(java.util.Arrays.asList(head1, head2), TwinEntity::getId);

            when(twinFieldDecimalRepository.sumChildrenTwinFieldValuesOfDivisionsByHead(
                    eq(kit.getIdSet()), eq(childStatusIds), eq(childOfClassIds),
                    eq(firstFieldId), eq(secondFieldId), eq(true), eq(false)))
                    .thenReturn(List.of(
                            new TwinFieldCalcProjection(head1.getId(), new BigDecimal("2.5")),
                            new TwinFieldCalcProjection(head2.getId(), new BigDecimal("10"))));

            storage.load(kit);

            // Class name says Divisions ByHead -> must call the *Divisions*ByHead variant, not link/subtraction/etc.
            verify(twinFieldDecimalRepository).sumChildrenTwinFieldValuesOfDivisionsByHead(
                    eq(kit.getIdSet()), eq(childStatusIds), eq(childOfClassIds),
                    eq(firstFieldId), eq(secondFieldId), eq(true), eq(false));
            verify(twinFieldDecimalRepository, never()).sumChildrenTwinFieldValuesOfSubtractionsByHead(
                    any(), any(), any(), any(), any(), anyBoolean());
            assertEquals(new BigDecimal("2.5"), head1.getTwinFieldCalculated().get(fieldId));
            assertEquals(new BigDecimal("10"), head2.getTwinFieldCalculated().get(fieldId));
        }

        @Test
        void load_twinWithoutProjection_defaultsToZero() throws ServiceException {
            var storage = newStorage(false, true);
            var head1 = twin(UUID.randomUUID());
            var head2 = twin(UUID.randomUUID());
            var kit = new Kit<>(java.util.Arrays.asList(head1, head2), TwinEntity::getId);

            when(twinFieldDecimalRepository.sumChildrenTwinFieldValuesOfDivisionsByHead(
                    any(), any(), any(), any(), any(), anyBoolean(), anyBoolean()))
                    .thenReturn(List.of(new TwinFieldCalcProjection(head1.getId(), new BigDecimal("6"))));

            storage.load(kit);

            assertEquals(new BigDecimal("6"), head1.getTwinFieldCalculated().get(fieldId));
            assertEquals(BigDecimal.ZERO, head2.getTwinFieldCalculated().get(fieldId));
        }

        @Test
        void load_passesDivisionByZeroIgnoreFlagThrough() throws ServiceException {
            var storage = newStorage(false, true);
            var head1 = twin(UUID.randomUUID());
            var kit = new Kit<>(java.util.List.of(head1), TwinEntity::getId);

            when(twinFieldDecimalRepository.sumChildrenTwinFieldValuesOfDivisionsByHead(
                    any(), any(), any(), any(), any(), anyBoolean(), eq(true)))
                    .thenReturn(List.of());

            storage.load(kit);

            verify(twinFieldDecimalRepository).sumChildrenTwinFieldValuesOfDivisionsByHead(
                    any(), any(), any(), any(), any(), anyBoolean(), eq(true));
        }
    }

    @Nested
    class EqualsAndMerge {

        @Test
        void equals_sameConfigDifferentInstance_isTrue() throws ServiceException {
            assertEquals(newStorage(true, false), newStorage(true, false));
        }

        @Test
        void equals_differentDivisionByZeroFlag_shouldBeFalse_butCurrentlyIsTrue() throws ServiceException {
            assertNotEquals(newStorage(true, false), newStorage(true, true));
        }

        @Test
        void equals_differentFirstField_isFalse() throws ServiceException {
            var a = newStorage(true, false);
            var b = new TwinFieldStorageCalcSumOfDivisionsByHead(
                    fieldId, twinFieldDecimalRepository, UUID.randomUUID(), secondFieldId,
                    childStatusIds, childOfClassIds, true, false);
            assertNotEquals(a, b);
        }
    }
}
