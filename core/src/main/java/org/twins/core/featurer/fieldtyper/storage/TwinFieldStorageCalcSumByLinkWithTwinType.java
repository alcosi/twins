package org.twins.core.featurer.fieldtyper.storage;

import org.cambium.common.exception.ServiceException;
import org.cambium.common.kit.Kit;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinFieldCalcProjection;
import org.twins.core.dao.twin.TwinFieldDecimalRepository;
import org.twins.core.service.auth.AuthService;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public class TwinFieldStorageCalcSumByLinkWithTwinType extends TwinFieldStorageCalc {

    private final TwinFieldDecimalRepository twinFieldDecimalRepository;
    private final AuthService authService;

    private final UUID twinClassFieldId;
    private final Set<UUID> linkIds;
    private final boolean srcElseDst;
    private final Set<UUID> linkedTwinInStatusIdSet;
    private final Set<UUID> linkedTwinOfClassIds;
    private final boolean statusExclude;
    private final String fieldIdByTwinFlavorId;
    private final boolean skipIfNotFound;
    private final boolean matchAssignee;

    public TwinFieldStorageCalcSumByLinkWithTwinType(
            UUID twinClassFieldId,
            TwinFieldDecimalRepository twinFieldDecimalRepository,
            AuthService authService,
            Set<UUID> linkIds,
            boolean srcElseDst,
            Set<UUID> linkedTwinInStatusIdSet,
            Set<UUID> linkedTwinOfClassIds,
            boolean statusExclude,
            String fieldIdByTwinFlavorId,
            boolean skipIfNotFound,
            boolean matchAssignee
    ) {
        super(twinClassFieldId);
        this.twinFieldDecimalRepository = twinFieldDecimalRepository;
        this.authService = authService;
        this.twinClassFieldId = twinClassFieldId;
        this.linkIds = linkIds;
        this.srcElseDst = srcElseDst;
        this.linkedTwinInStatusIdSet = linkedTwinInStatusIdSet;
        this.linkedTwinOfClassIds = linkedTwinOfClassIds;
        this.statusExclude = statusExclude;
        this.fieldIdByTwinFlavorId = fieldIdByTwinFlavorId;
        this.skipIfNotFound = skipIfNotFound;
        this.matchAssignee = matchAssignee;
    }

    @Override
    public void load(Kit<TwinEntity, UUID> twinsKit) throws ServiceException {
        UUID assigneeUserId = resolveAssigneeUserId();
        List<TwinFieldCalcProjection> calc = twinFieldDecimalRepository.sumLinkedTwinFieldValuesByLinkWithTwinFlavor(
                twinsKit.getIdSet(),
                linkIds,
                fieldIdByTwinFlavorId,
                srcElseDst,
                linkedTwinInStatusIdSet,
                linkedTwinOfClassIds,
                statusExclude,
                skipIfNotFound,
                assigneeUserId
        );

        packResult(twinsKit, calc);
    }

    private UUID resolveAssigneeUserId() throws ServiceException {
        if (!matchAssignee) {
            return null;
        }
        return authService.getApiUser().getUserId();
    }

    @Override
    boolean canBeMerged(Object o) {
        if (!isSameClass(o)) {
            return false;
        }
        TwinFieldStorageCalcSumByLinkWithTwinType other = (TwinFieldStorageCalcSumByLinkWithTwinType) o;
        return Objects.equals(twinClassFieldId, other.twinClassFieldId)
                && Objects.equals(linkIds, other.linkIds)
                && Objects.equals(srcElseDst, other.srcElseDst)
                && Objects.equals(linkedTwinInStatusIdSet, other.linkedTwinInStatusIdSet)
                && Objects.equals(linkedTwinOfClassIds, other.linkedTwinOfClassIds)
                && Objects.equals(statusExclude, other.statusExclude)
                && Objects.equals(fieldIdByTwinFlavorId, other.fieldIdByTwinFlavorId)
                && Objects.equals(skipIfNotFound, other.skipIfNotFound)
                && Objects.equals(matchAssignee, other.matchAssignee);
    }
}
