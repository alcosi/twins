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
import org.twins.core.featurer.fieldtyper.storage.TwinFieldStorageCalcSumChildrenOfLinked;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class TwinFieldStorageCalcSumChildrenOfLinkedTest extends BaseUnitTest {

    @Mock
    private TwinFieldDecimalRepository twinFieldDecimalRepository;

    private UUID fieldId;
    private Set<UUID> fieldIds;
    private Set<UUID> linkIds;
    private Set<UUID> linkedInStatusIds;
    private Set<UUID> linkedOfClassIds;
    private Set<UUID> childrenOfClassIds;
    private Set<UUID> childrenInStatusIds;

    @BeforeEach
    void setUp() {
        fieldId = UUID.randomUUID();
        fieldIds = Set.of(UUID.randomUUID());
        linkIds = Set.of(UUID.randomUUID());
        linkedInStatusIds = Set.of(UUID.randomUUID());
        linkedOfClassIds = Set.of(UUID.randomUUID());
        childrenOfClassIds = Set.of(UUID.randomUUID());
        childrenInStatusIds = Set.of(UUID.randomUUID());
    }

    private TwinFieldStorageCalcSumChildrenOfLinked newStorage(boolean srcElseDst, boolean linkedExclude, boolean childrenExclude) {
        return new TwinFieldStorageCalcSumChildrenOfLinked(
                fieldId,
                twinFieldDecimalRepository,
                fieldIds,
                linkIds,
                srcElseDst,
                linkedInStatusIds,
                linkedOfClassIds,
                linkedExclude,
                childrenOfClassIds,
                childrenInStatusIds,
                childrenExclude
        );
    }

    private TwinEntity twin(UUID id) {
        var t = new TwinEntity();
        t.setId(id);
        return t;
    }

    @Nested
    class Load {

        @Test
        void load_delegatesToChildrenOfLinkedRepository_andPacksProjectedSums() throws ServiceException {
            var storage = newStorage(true, false, false);
            var dst1 = twin(UUID.randomUUID());
            var dst2 = twin(UUID.randomUUID());
            var kit = new Kit<>(java.util.Arrays.asList(dst1, dst2), TwinEntity::getId);

            when(twinFieldDecimalRepository.sumChildrenFieldsOfLinkedTwins(
                    eq(kit.getIdSet()), eq(true), eq(linkedInStatusIds), eq(linkedOfClassIds), eq(false),
                    eq(linkIds), eq(childrenOfClassIds), eq(childrenInStatusIds), eq(false), eq(fieldIds)))
                    .thenReturn(List.of(
                            new TwinFieldCalcProjection(dst1.getId(), new BigDecimal("3")),
                            new TwinFieldCalcProjection(dst2.getId(), new BigDecimal("4"))));

            storage.load(kit);

            verify(twinFieldDecimalRepository).sumChildrenFieldsOfLinkedTwins(
                    eq(kit.getIdSet()), eq(true), eq(linkedInStatusIds), eq(linkedOfClassIds), eq(false),
                    eq(linkIds), eq(childrenOfClassIds), eq(childrenInStatusIds), eq(false), eq(fieldIds));
            verify(twinFieldDecimalRepository, never()).sumLinkedTwinFieldValuesByLink(
                    any(), anyBoolean(), any(), any(), any(), any(), anyBoolean());
            assertEquals(new BigDecimal("3"), dst1.getTwinFieldCalculated().get(fieldId));
            assertEquals(new BigDecimal("4"), dst2.getTwinFieldCalculated().get(fieldId));
        }

        @Test
        void load_twinWithoutProjection_defaultsToZero() throws ServiceException {
            var storage = newStorage(false, true, true);
            var dst1 = twin(UUID.randomUUID());
            var dst2 = twin(UUID.randomUUID());
            var kit = new Kit<>(java.util.Arrays.asList(dst1, dst2), TwinEntity::getId);

            when(twinFieldDecimalRepository.sumChildrenFieldsOfLinkedTwins(
                    any(), anyBoolean(), any(), any(), anyBoolean(), any(), any(), any(), anyBoolean(), any()))
                    .thenReturn(List.of(new TwinFieldCalcProjection(dst1.getId(), new BigDecimal("11"))));

            storage.load(kit);

            assertEquals(new BigDecimal("11"), dst1.getTwinFieldCalculated().get(fieldId));
            assertEquals(BigDecimal.ZERO, dst2.getTwinFieldCalculated().get(fieldId));
        }
    }
}
