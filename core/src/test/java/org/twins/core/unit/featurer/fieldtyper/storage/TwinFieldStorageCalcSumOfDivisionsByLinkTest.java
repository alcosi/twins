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
import org.twins.core.featurer.fieldtyper.storage.TwinFieldStorageCalcSumOfDivisionsByLink;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.mockito.Mockito.*;

class TwinFieldStorageCalcSumOfDivisionsByLinkTest extends BaseUnitTest {

    @Mock
    private TwinFieldDecimalRepository twinFieldDecimalRepository;

    private UUID fieldId;
    private UUID firstFieldId;
    private UUID secondFieldId;
    private UUID linkId;
    private Set<UUID> linkedInStatusIds;
    private Set<UUID> linkedOfClassIds;

    @BeforeEach
    void setUp() {
        fieldId = UUID.randomUUID();
        firstFieldId = UUID.randomUUID();
        secondFieldId = UUID.randomUUID();
        linkId = UUID.randomUUID();
        linkedInStatusIds = Set.of(UUID.randomUUID());
        linkedOfClassIds = Set.of(UUID.randomUUID());
    }

    private TwinFieldStorageCalcSumOfDivisionsByLink newStorage(boolean srcElseDst, boolean statusExclude, boolean divisionByZeroIgnore) {
        return new TwinFieldStorageCalcSumOfDivisionsByLink(
                fieldId, twinFieldDecimalRepository, firstFieldId, secondFieldId, linkId,
                srcElseDst, linkedInStatusIds, linkedOfClassIds, statusExclude, divisionByZeroIgnore);
    }

    private TwinEntity twin(UUID id) {
        var t = new TwinEntity();
        t.setId(id);
        return t;
    }

    @Nested
    class Load {

        @Test
        void load_delegatesToDivisionsByLinkRepository_andPacksResults() throws ServiceException {
            var storage = newStorage(true, false, true);
            var dst1 = twin(UUID.randomUUID());
            var dst2 = twin(UUID.randomUUID());
            var kit = new Kit<>(java.util.Arrays.asList(dst1, dst2), TwinEntity::getId);

            when(twinFieldDecimalRepository.sumLinkedTwinFieldValuesOfDivisionsByLink(
                    eq(kit.getIdSet()), eq(true), eq(linkedInStatusIds), eq(linkedOfClassIds),
                    eq(firstFieldId), eq(secondFieldId), eq(false), eq(true)))
                    .thenReturn(List.of(
                            new TwinFieldCalcProjection(dst1.getId(), new BigDecimal("2")),
                            new TwinFieldCalcProjection(dst2.getId(), new BigDecimal("5"))));

            storage.load(kit);

            // Class name says Divisions ByLink -> must call the *Divisions*ByLink variant.
            verify(twinFieldDecimalRepository).sumLinkedTwinFieldValuesOfDivisionsByLink(
                    eq(kit.getIdSet()), eq(true), eq(linkedInStatusIds), eq(linkedOfClassIds),
                    eq(firstFieldId), eq(secondFieldId), eq(false), eq(true));
            verify(twinFieldDecimalRepository, never()).sumLinkedTwinFieldValuesOfMultiplicationsByLink(
                    any(), anyBoolean(), any(), any(), any(), any(), anyBoolean());
            assertEquals(new BigDecimal("2"), dst1.getTwinFieldCalculated().get(fieldId));
            assertEquals(new BigDecimal("5"), dst2.getTwinFieldCalculated().get(fieldId));
        }

        @Test
        void load_twinWithoutProjection_defaultsToZero() throws ServiceException {
            var storage = newStorage(false, true, false);
            var dst1 = twin(UUID.randomUUID());
            var dst2 = twin(UUID.randomUUID());
            var kit = new Kit<>(java.util.Arrays.asList(dst1, dst2), TwinEntity::getId);

            when(twinFieldDecimalRepository.sumLinkedTwinFieldValuesOfDivisionsByLink(
                    any(), anyBoolean(), any(), any(), any(), any(), anyBoolean(), anyBoolean()))
                    .thenReturn(List.of(new TwinFieldCalcProjection(dst1.getId(), new BigDecimal("8"))));

            storage.load(kit);

            assertEquals(new BigDecimal("8"), dst1.getTwinFieldCalculated().get(fieldId));
            assertEquals(BigDecimal.ZERO, dst2.getTwinFieldCalculated().get(fieldId));
        }
    }

    @Nested
    class EqualsAndMerge {

        @Test
        void equals_sameConfigDifferentInstance_isTrue() throws ServiceException {
            assertEquals(newStorage(true, false, true), newStorage(true, false, true));
        }

        @Test
        void equals_differentLinkId_isFalse() throws ServiceException {
            var a = newStorage(true, false, true);
            var b = new TwinFieldStorageCalcSumOfDivisionsByLink(
                    fieldId, twinFieldDecimalRepository, firstFieldId, secondFieldId, UUID.randomUUID(),
                    true, linkedInStatusIds, linkedOfClassIds, false, true);
            assertNotEquals(a, b);
        }

        @Test
        void equals_differentDivisionByZeroFlag_isFalse() throws ServiceException {
            assertNotEquals(newStorage(true, false, false), newStorage(true, false, true));
        }
    }
}
