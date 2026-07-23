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
import org.twins.core.domain.ApiUser;
import org.twins.core.featurer.fieldtyper.storage.TwinFieldStorageCalcSumByLink;
import org.twins.core.service.auth.AuthService;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TwinFieldStorageCalcSumByLinkTest extends BaseUnitTest {

    @Mock
    private TwinFieldDecimalRepository twinFieldDecimalRepository;

    @Mock
    private AuthService authService;

    @Mock
    private ApiUser apiUser;

    private UUID fieldId;
    private Set<UUID> linkedTwinClassIds;
    private Set<UUID> linkIds;
    private Set<UUID> linkedInStatusIds;
    private Set<UUID> linkedOfClassIds;

    @BeforeEach
    void setUp() throws ServiceException {
        fieldId = UUID.randomUUID();
        linkedTwinClassIds = Set.of(UUID.randomUUID());
        linkIds = Set.of(UUID.randomUUID());
        linkedInStatusIds = Set.of(UUID.randomUUID());
        linkedOfClassIds = Set.of(UUID.randomUUID());
        when(authService.getApiUser()).thenReturn(apiUser);
        when(apiUser.getUserId()).thenReturn(null);
    }

    private TwinFieldStorageCalcSumByLink newStorage(boolean srcElseDst, boolean statusExclude) {
        return newStorage(srcElseDst, statusExclude, false);
    }

    private TwinFieldStorageCalcSumByLink newStorage(boolean srcElseDst, boolean statusExclude, boolean matchAssignee) {
        return new TwinFieldStorageCalcSumByLink(
                fieldId, twinFieldDecimalRepository, authService, linkedTwinClassIds,
                linkedInStatusIds, linkedOfClassIds, srcElseDst, statusExclude, linkIds, matchAssignee);
    }

    private TwinEntity twin(UUID id) {
        var t = new TwinEntity();
        t.setId(id);
        return t;
    }

    @Nested
    class Load {

        @Test
        void load_delegatesToByLinkRepository_andPacksProjectedSums() throws ServiceException {
            var storage = newStorage(true, false);
            var dst1 = twin(UUID.randomUUID());
            var dst2 = twin(UUID.randomUUID());
            var kit = new Kit<>(java.util.Arrays.asList(dst1, dst2), TwinEntity::getId);

            when(twinFieldDecimalRepository.sumLinkedTwinFieldValuesByLink(
                    eq(kit.getIdSet()), eq(true), eq(linkedInStatusIds),
                    eq(linkedOfClassIds), eq(linkedTwinClassIds), eq(linkIds), eq(false), isNull()))
                    .thenReturn(List.of(
                            new TwinFieldCalcProjection(dst1.getId(), new BigDecimal("3")),
                            new TwinFieldCalcProjection(dst2.getId(), new BigDecimal("4"))));

            storage.load(kit);

            // ByLink: aggregation targets the link relation, NOT the head/children relation.
            verify(twinFieldDecimalRepository).sumLinkedTwinFieldValuesByLink(
                    eq(kit.getIdSet()), eq(true), eq(linkedInStatusIds),
                    eq(linkedOfClassIds), eq(linkedTwinClassIds), eq(linkIds), eq(false), isNull());
            verify(twinFieldDecimalRepository, never()).sumChildrenTwinFieldValuesByHead(
                    any(), any(), any(), anyBoolean(), any());
            assertEquals(new BigDecimal("3"), dst1.getTwinFieldCalculated().get(fieldId));
            assertEquals(new BigDecimal("4"), dst2.getTwinFieldCalculated().get(fieldId));
        }

        @Test
        void load_matchAssigneeTrue_passesApiUserId() throws ServiceException {
            var userId = UUID.randomUUID();
            when(apiUser.getUserId()).thenReturn(userId);
            var storage = newStorage(true, false, true);
            var dst1 = twin(UUID.randomUUID());
            var kit = new Kit<>(java.util.List.of(dst1), TwinEntity::getId);

            when(twinFieldDecimalRepository.sumLinkedTwinFieldValuesByLink(
                    any(), anyBoolean(), any(), any(), any(), any(), anyBoolean(), eq(userId)))
                    .thenReturn(List.of());

            storage.load(kit);

            verify(twinFieldDecimalRepository).sumLinkedTwinFieldValuesByLink(
                    eq(kit.getIdSet()), eq(true), eq(linkedInStatusIds),
                    eq(linkedOfClassIds), eq(linkedTwinClassIds), eq(linkIds), eq(false), eq(userId));
        }

        @Test
        void load_twinWithoutProjection_defaultsToZero() throws ServiceException {
            var storage = newStorage(false, true);
            var dst1 = twin(UUID.randomUUID());
            var dst2 = twin(UUID.randomUUID());
            var kit = new Kit<>(java.util.Arrays.asList(dst1, dst2), TwinEntity::getId);

            when(twinFieldDecimalRepository.sumLinkedTwinFieldValuesByLink(
                    any(), anyBoolean(), any(), any(), any(), any(), anyBoolean(), any()))
                    .thenReturn(List.of(new TwinFieldCalcProjection(dst1.getId(), new BigDecimal("11"))));

            storage.load(kit);

            assertEquals(new BigDecimal("11"), dst1.getTwinFieldCalculated().get(fieldId));
            assertEquals(BigDecimal.ZERO, dst2.getTwinFieldCalculated().get(fieldId));
        }

        @Test
        void load_emptyProjections_yieldsZeroForEveryTwin() throws ServiceException {
            var storage = newStorage(false, false);
            var dst1 = twin(UUID.randomUUID());
            var kit = new Kit<>(java.util.List.of(dst1), TwinEntity::getId);

            when(twinFieldDecimalRepository.sumLinkedTwinFieldValuesByLink(
                    any(), anyBoolean(), any(), any(), any(), any(), anyBoolean(), any()))
                    .thenReturn(List.of());

            storage.load(kit);

            assertEquals(BigDecimal.ZERO, dst1.getTwinFieldCalculated().get(fieldId));
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
            // ByLink merge contract must include the linkId (distinguishes which link aggregates).
            var a = newStorage(true, false);
            var b = new TwinFieldStorageCalcSumByLink(
                    fieldId, twinFieldDecimalRepository, authService, linkedTwinClassIds,
                    linkedInStatusIds, linkedOfClassIds, true, false, Set.of(UUID.randomUUID()), false);
            assertNotEquals(a, b);
        }

        @Test
        void equals_differentSrcElseDst_isFalse() throws ServiceException {
            assertNotEquals(newStorage(true, false), newStorage(false, false));
        }
    }
}
