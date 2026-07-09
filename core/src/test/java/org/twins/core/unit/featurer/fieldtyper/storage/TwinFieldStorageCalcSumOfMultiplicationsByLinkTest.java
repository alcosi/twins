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
import org.twins.core.featurer.fieldtyper.storage.TwinFieldStorageCalcSumOfMultiplicationsByLink;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TwinFieldStorageCalcSumOfMultiplicationsByLinkTest extends BaseUnitTest {

    @Mock
    private TwinFieldDecimalRepository twinFieldDecimalRepository;

    private UUID fieldId;
    private UUID firstFieldId;
    private UUID secondFieldId;
    private Set<UUID> linkIds;
    private Set<UUID> linkedInStatusIds;
    private Set<UUID> linkedOfClassIds;

    @BeforeEach
    void setUp() {
        fieldId = UUID.randomUUID();
        firstFieldId = UUID.randomUUID();
        secondFieldId = UUID.randomUUID();
        linkIds = Set.of(UUID.randomUUID());
        linkedInStatusIds = Set.of(UUID.randomUUID());
        linkedOfClassIds = Set.of(UUID.randomUUID());
    }

    private TwinFieldStorageCalcSumOfMultiplicationsByLink newStorage(boolean srcElseDst, boolean statusExclude) {
        return new TwinFieldStorageCalcSumOfMultiplicationsByLink(
                fieldId, twinFieldDecimalRepository, firstFieldId, secondFieldId, linkIds,
                srcElseDst, linkedInStatusIds, linkedOfClassIds, statusExclude);
    }

    private TwinEntity twin(UUID id) {
        var t = new TwinEntity();
        t.setId(id);
        return t;
    }

    @Nested
    class Load {

        @Test
        void load_delegatesToMultiplicationsByLinkRepository_andPacksResults() throws ServiceException {
            var storage = newStorage(true, false);
            var dst1 = twin(UUID.randomUUID());
            var dst2 = twin(UUID.randomUUID());
            var kit = new Kit<>(java.util.Arrays.asList(dst1, dst2), TwinEntity::getId);

            when(twinFieldDecimalRepository.sumLinkedTwinFieldValuesOfMultiplicationsByLink(
                    eq(kit.getIdSet()), eq(true), eq(linkedInStatusIds), eq(linkedOfClassIds),
                    eq(firstFieldId), eq(secondFieldId), eq(linkIds), eq(false)))
                    .thenReturn(List.of(
                            new TwinFieldCalcProjection(dst1.getId(), new BigDecimal("9")),
                            new TwinFieldCalcProjection(dst2.getId(), new BigDecimal("1"))));

            storage.load(kit);

            verify(twinFieldDecimalRepository).sumLinkedTwinFieldValuesOfMultiplicationsByLink(
                    eq(kit.getIdSet()), eq(true), eq(linkedInStatusIds), eq(linkedOfClassIds),
                    eq(firstFieldId), eq(secondFieldId), eq(linkIds), eq(false));
            verify(twinFieldDecimalRepository, never()).sumLinkedTwinFieldValuesOfSubtractionsByLink(
                    any(), anyBoolean(), any(), any(), any(), any(), any(), anyBoolean());
            assertEquals(new BigDecimal("9"), dst1.getTwinFieldCalculated().get(fieldId));
            assertEquals(new BigDecimal("1"), dst2.getTwinFieldCalculated().get(fieldId));
        }

        @Test
        void load_twinWithoutProjection_defaultsToZero() throws ServiceException {
            var storage = newStorage(false, true);
            var dst1 = twin(UUID.randomUUID());
            var dst2 = twin(UUID.randomUUID());
            var kit = new Kit<>(java.util.Arrays.asList(dst1, dst2), TwinEntity::getId);

            when(twinFieldDecimalRepository.sumLinkedTwinFieldValuesOfMultiplicationsByLink(
                    any(), anyBoolean(), any(), any(), any(), any(), any(), anyBoolean()))
                    .thenReturn(List.of(new TwinFieldCalcProjection(dst1.getId(), new BigDecimal("15"))));

            storage.load(kit);

            assertEquals(new BigDecimal("15"), dst1.getTwinFieldCalculated().get(fieldId));
            assertEquals(BigDecimal.ZERO, dst2.getTwinFieldCalculated().get(fieldId));
        }
    }

    @Nested
    class EqualsAndMerge {

        @Test
        void equals_sameConfigDifferentInstance_isTrue() throws ServiceException {
            assertEquals(newStorage(true, false), newStorage(true, false));
        }

        @Test
        void equals_differentLinkId_isFalse() throws ServiceException {
            var a = newStorage(true, false);
            var b = new TwinFieldStorageCalcSumOfMultiplicationsByLink(
                    fieldId, twinFieldDecimalRepository, firstFieldId, secondFieldId, Set.of(UUID.randomUUID()),
                    true, linkedInStatusIds, linkedOfClassIds, false);
            assertNotEquals(a, b);
        }
    }
}
