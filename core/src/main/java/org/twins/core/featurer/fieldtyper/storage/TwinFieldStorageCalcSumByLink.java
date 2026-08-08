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

public class TwinFieldStorageCalcSumByLink extends TwinFieldStorageCalc {

    private final TwinFieldDecimalRepository twinFieldDecimalRepository;
    private final AuthService authService;
    private final Set<UUID> linkedTwinClassIds;
    private final Set<UUID> linkIds;
    private final boolean srcElseDst;
    private final Set<UUID> linkedTwinInStatusIdList;
    private final Set<UUID> linkedTwinOfClassIds;
    private final boolean statusExclude;
    private final boolean matchAssignee;

    public TwinFieldStorageCalcSumByLink(UUID twinClassFieldId, TwinFieldDecimalRepository twinFieldDecimalRepository, AuthService authService, Set<UUID> linkedTwinClassIds, Set<UUID> linkedTwinInStatusIdList, Set<UUID> linkedTwinOfClassIds, boolean srcElseDst, boolean statusExclude, Set<UUID> linkIds, boolean matchAssignee) {
        super(twinClassFieldId);
        this.twinFieldDecimalRepository = twinFieldDecimalRepository;
        this.authService = authService;
        this.linkedTwinClassIds = linkedTwinClassIds;
        this.linkIds = linkIds;
        this.srcElseDst = srcElseDst;
        this.linkedTwinInStatusIdList = linkedTwinInStatusIdList;
        this.linkedTwinOfClassIds = linkedTwinOfClassIds;
        this.statusExclude = statusExclude;
        this.matchAssignee = matchAssignee;
    }

    @Override
    public void load(Kit<TwinEntity, UUID> twinsKit) throws ServiceException {
        UUID assigneeUserId = resolveAssigneeUserId();
        List<TwinFieldCalcProjection> calc = twinFieldDecimalRepository.sumLinkedTwinFieldValuesByLink(
                twinsKit.getIdSet(),
                srcElseDst,
                linkedTwinInStatusIdList,
                linkedTwinOfClassIds,
                linkedTwinClassIds,
                linkIds,
                statusExclude,
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
        return isSameClass(o)
                && Objects.equals(this.twinClassFieldId, ((TwinFieldStorageCalcSumByLink) o).twinClassFieldId)
                && Objects.equals(this.linkedTwinClassIds, ((TwinFieldStorageCalcSumByLink) o).linkedTwinClassIds)
                && Objects.equals(this.linkIds, ((TwinFieldStorageCalcSumByLink) o).linkIds)
                && Objects.equals(this.srcElseDst, ((TwinFieldStorageCalcSumByLink) o).srcElseDst)
                && Objects.equals(this.linkedTwinInStatusIdList, ((TwinFieldStorageCalcSumByLink) o).linkedTwinInStatusIdList)
                && Objects.equals(this.linkedTwinOfClassIds, ((TwinFieldStorageCalcSumByLink) o).linkedTwinOfClassIds)
                && Objects.equals(this.statusExclude, ((TwinFieldStorageCalcSumByLink) o).statusExclude)
                && Objects.equals(this.matchAssignee, ((TwinFieldStorageCalcSumByLink) o).matchAssignee);
    }
}
