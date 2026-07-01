package org.twins.core.featurer.fieldtyper.storage;

import org.cambium.common.kit.Kit;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinFieldCalcProjection;
import org.twins.core.dao.twin.TwinRepository;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public class TwinFieldStorageCalcBackwardLinkedTwinCount extends TwinFieldStorageCalc {
    private final TwinRepository twinRepository;
    private final Set<UUID> linkIds;
    private final Set<UUID> linkedTwinStatusIdSet;
    private final boolean exclude;

    public TwinFieldStorageCalcBackwardLinkedTwinCount(
            TwinRepository twinRepository,
            UUID twinClassFieldId,
            Set<UUID> linkIds,
            Set<UUID> linkedTwinStatusIdSet,
            boolean exclude,
            UUID calcUserId,
            UUID calcUserGroupFootprintId) {
        super(twinClassFieldId, calcUserId, calcUserGroupFootprintId);
        this.twinRepository = twinRepository;
        this.linkIds = linkIds;
        this.linkedTwinStatusIdSet = linkedTwinStatusIdSet;
        this.exclude = exclude;
    }

    @Override
    public void load(Kit<TwinEntity, UUID> twinsKit) {
        List<TwinFieldCalcProjection> calc = exclude ?
                twinRepository.countLinkedTwinsByBackwardLinkWithStatusNotIn(twinsKit.getIdSet(), linkIds, linkedTwinStatusIdSet) :
                twinRepository.countLinkedTwinsByBackwardLinkWithStatusIn(twinsKit.getIdSet(), linkIds, linkedTwinStatusIdSet);
        packResult(twinsKit, calc);
    }

    @Override
    boolean canBeMerged(Object o) {
        return isSameClass(o)
                && Objects.equals(this.twinClassFieldId, ((TwinFieldStorageCalcBackwardLinkedTwinCount) o).twinClassFieldId)
                && Objects.equals(this.linkIds, ((TwinFieldStorageCalcBackwardLinkedTwinCount) o).linkIds)
                && Objects.equals(this.linkedTwinStatusIdSet, ((TwinFieldStorageCalcBackwardLinkedTwinCount) o).linkedTwinStatusIdSet)
                && Objects.equals(this.exclude, ((TwinFieldStorageCalcBackwardLinkedTwinCount) o).exclude)
                && hasSameCalcPermissionContext((TwinFieldStorageCalcBackwardLinkedTwinCount) o);
    }
}
