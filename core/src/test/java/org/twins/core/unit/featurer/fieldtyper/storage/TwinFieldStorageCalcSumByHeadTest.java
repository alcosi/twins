package org.twins.core.unit.featurer.fieldtyper.storage;

import org.cambium.common.exception.ServiceException;
import org.cambium.common.kit.Kit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinFieldCalcProjection;
import org.twins.core.dao.twin.TwinFieldDecimalRepository;
import org.twins.core.featurer.fieldtyper.storage.TwinFieldStorageCalcSumByHead;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TwinFieldStorageCalcSumByHeadTest extends BaseUnitTest {

    @Mock
    private TwinFieldDecimalRepository twinFieldDecimalRepository;

    private UUID fieldId;
    private Set<UUID> childFieldIds;
    private Set<UUID> childStatusIds;
    private Set<UUID> childOfClassIds;

    @BeforeEach
    void setUp() {
        fieldId = UUID.randomUUID();
        childFieldIds = Set.of(UUID.randomUUID());
        childStatusIds = Set.of(UUID.randomUUID());
        childOfClassIds = Set.of(UUID.randomUUID());
    }

    private TwinFieldStorageCalcSumByHead newStorage(boolean exclude) {
        return new TwinFieldStorageCalcSumByHead(
                twinFieldDecimalRepository,
                fieldId,
                childFieldIds,
                childStatusIds,
                childOfClassIds,
                exclude);
    }

    private TwinEntity twin(UUID id) {
        var t = new TwinEntity();
        t.setId(id);
        return t;
    }

    @Nested
    class Load {

        @Test
        void load_delegatesToByHeadRepository_andPacksProjectedSums() throws ServiceException {
            var storage = newStorage(true);
            var head1 = twin(UUID.randomUUID());
            var head2 = twin(UUID.randomUUID());
            var kit = new Kit<>(java.util.Arrays.asList(head1, head2), TwinEntity::getId);

            var sum1 = new BigDecimal("12.50");
            var sum2 = new BigDecimal("7.00");
            when(twinFieldDecimalRepository.sumChildrenTwinFieldValuesByHead(
                    eq(kit.getIdSet()), eq(childFieldIds), eq(childStatusIds), eq(true), eq(childOfClassIds)))
                    .thenReturn(List.of(
                            new TwinFieldCalcProjection(head1.getId(), sum1),
                            new TwinFieldCalcProjection(head2.getId(), sum2)));

            storage.load(kit);

            // ByHead: aggregation targets the head/children relation, NOT a link.
            verify(twinFieldDecimalRepository).sumChildrenTwinFieldValuesByHead(
                    eq(kit.getIdSet()), eq(childFieldIds), eq(childStatusIds), eq(true), eq(childOfClassIds));
            verify(twinFieldDecimalRepository, never()).sumLinkedTwinFieldValuesByLink(
                    any(), anyBoolean(), any(), any(), any(), anyBoolean());
            assertEquals(sum1, head1.getTwinFieldCalculated().get(fieldId));
            assertEquals(sum2, head2.getTwinFieldCalculated().get(fieldId));
        }

        @Test
        void load_twinWithoutProjection_defaultsToZero() throws ServiceException {
            var storage = newStorage(false);
            var head1 = twin(UUID.randomUUID());
            var head2 = twin(UUID.randomUUID());
            var kit = new Kit<>(java.util.Arrays.asList(head1, head2), TwinEntity::getId);

            when(twinFieldDecimalRepository.sumChildrenTwinFieldValuesByHead(
                    any(), any(), any(), anyBoolean(), any()))
                    .thenReturn(List.of(new TwinFieldCalcProjection(head1.getId(), new BigDecimal("9"))));

            storage.load(kit);

            assertEquals(new BigDecimal("9"), head1.getTwinFieldCalculated().get(fieldId));
            // head2 had no projection -> must default to ZERO, never null
            assertEquals(BigDecimal.ZERO, head2.getTwinFieldCalculated().get(fieldId));
        }

        @Test
        void load_emptyProjections_yieldsZeroForEveryTwin() throws ServiceException {
            var storage = newStorage(false);
            var head1 = twin(UUID.randomUUID());
            var kit = new Kit<>(java.util.List.of(head1), TwinEntity::getId);

            when(twinFieldDecimalRepository.sumChildrenTwinFieldValuesByHead(
                    any(), any(), any(), anyBoolean(), any()))
                    .thenReturn(List.of());

            storage.load(kit);

            assertEquals(BigDecimal.ZERO, head1.getTwinFieldCalculated().get(fieldId));
        }

        @Test
        void load_passesExcludeFlagThroughUnchanged() throws ServiceException {
            var storage = newStorage(true);
            var head1 = twin(UUID.randomUUID());
            var kit = new Kit<>(java.util.List.of(head1), TwinEntity::getId);

            when(twinFieldDecimalRepository.sumChildrenTwinFieldValuesByHead(
                    any(), any(), any(), eq(true), any()))
                    .thenReturn(List.of());

            storage.load(kit);

            ArgumentCaptor<Boolean> exclude = ArgumentCaptor.forClass(Boolean.class);
            verify(twinFieldDecimalRepository).sumChildrenTwinFieldValuesByHead(
                    any(), any(), any(), exclude.capture(), any());
            assertTrue(exclude.getValue());
        }
    }

    @Nested
    class EqualsAndMerge {

        @Test
        void equals_sameConfigDifferentInstance_isTrue() throws ServiceException {
            // Two ByHead storages with identical config must merge/compare equal
            var a = newStorage(true);
            var b = newStorage(true);
            assertEquals(a, b);
        }

        @Test
        void equals_differentExclude_isFalse() throws ServiceException {
            assertNotEquals(newStorage(true), newStorage(false));
        }

        @Test
        void equals_differentTwinClassField_isFalse() throws ServiceException {
            var a = newStorage(true);
            var b = new TwinFieldStorageCalcSumByHead(
                    twinFieldDecimalRepository, UUID.randomUUID(),
                    childFieldIds, childStatusIds, childOfClassIds, true);
            assertNotEquals(a, b);
        }
    }

    @Nested
    class CalculatedFlags {

        @Test
        void hasStrictValues_isAlwaysFalse() throws ServiceException {
            assertFalse(newStorage(false).hasStrictValues(fieldId));
        }

        @Test
        void isLoaded_reflectsCalculatedMapPresence() throws ServiceException {
            var storage = newStorage(false);
            var t = twin(UUID.randomUUID());
            assertFalse(storage.isLoaded(t));
            storage.initEmpty(t);
            assertTrue(storage.isLoaded(t));
        }

        @Test
        void initEmpty_setsZeroWithoutDiscardingExistingCalculatedEntries() throws ServiceException {
            var storage = newStorage(false);
            var t = twin(UUID.randomUUID());
            var otherField = UUID.randomUUID();
            t.setTwinFieldCalculated(new java.util.HashMap<>(java.util.Map.of(otherField, BigDecimal.TEN)));

            storage.initEmpty(t);

            assertEquals(BigDecimal.TEN, t.getTwinFieldCalculated().get(otherField));
            assertEquals(BigDecimal.ZERO, t.getTwinFieldCalculated().get(fieldId));
        }
    }
}
