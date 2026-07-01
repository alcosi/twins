package org.twins.core.featurer.fieldtyper.storage;

import org.cambium.common.kit.Kit;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinFieldCalcProjection;
import org.twins.core.dao.twin.TwinFieldSimpleRepository;
import org.twins.core.dao.twin.TwinRepository;
import org.twins.core.dao.twinclass.TwinClassFieldRepository;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public class TwinFieldStorageCalcChildrenInStatusCount extends TwinFieldStorageCalc {
    private final TwinRepository twinRepository;
    private final Set<UUID> childrenTwinStatusIdSet;
    private final boolean exclude;

    public TwinFieldStorageCalcChildrenInStatusCount(TwinRepository twinRepository, UUID twinClassFieldId, Set<UUID> childrenTwinStatusIdSet, boolean exclude, UUID calcUserId, UUID calcUserGroupFootprintId) {
        super(twinClassFieldId, calcUserId, calcUserGroupFootprintId);
        this.twinRepository = twinRepository;
        this.childrenTwinStatusIdSet = childrenTwinStatusIdSet;
        this.exclude = exclude;
    }

    @Override
    public void load(Kit<TwinEntity, UUID> twinsKit) {
        List<TwinFieldCalcProjection> calc = exclude ?
                twinRepository.countChildrenTwinsWithStatusNotIn(twinsKit.getIdSet(), childrenTwinStatusIdSet, calcUserId, calcUserGroupFootprintId) :
                twinRepository.countChildrenTwinsWithStatusIn(twinsKit.getIdSet(), childrenTwinStatusIdSet, calcUserId, calcUserGroupFootprintId);
        packResult(twinsKit, calc);
    }

    @Override
    boolean canBeMerged(Object o) {
        return isSameClass(o)
                && Objects.equals(this.twinClassFieldId, ((TwinFieldStorageCalcChildrenInStatusCount) o).twinClassFieldId)
                && Objects.equals(this.childrenTwinStatusIdSet, ((TwinFieldStorageCalcChildrenInStatusCount) o).childrenTwinStatusIdSet)
                && Objects.equals(this.exclude, ((TwinFieldStorageCalcChildrenInStatusCount) o).exclude)
                && hasSameCalcPermissionContext((TwinFieldStorageCalcChildrenInStatusCount) o);
    }
}
