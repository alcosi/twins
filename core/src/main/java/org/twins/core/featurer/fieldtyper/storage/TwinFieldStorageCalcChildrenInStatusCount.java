package org.twins.core.featurer.fieldtyper.storage;

import org.cambium.common.kit.Kit;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinFieldCalcProjection;
import org.twins.core.dao.twin.TwinFieldSimpleRepository;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public class TwinFieldStorageCalcChildrenInStatusCount extends TwinFieldStorageCalc {
    private final TwinFieldSimpleRepository twinFieldSimpleRepository;
    private final Set<UUID> childrenTwinStatusIdSet;
    private final boolean exclude;

    public TwinFieldStorageCalcChildrenInStatusCount(TwinFieldSimpleRepository twinFieldSimpleRepository, UUID twinClassFieldId, Set<UUID> childrenTwinStatusIdSet, boolean exclude) {
        super(twinClassFieldId);
        this.twinFieldSimpleRepository = twinFieldSimpleRepository;
        this.childrenTwinStatusIdSet = childrenTwinStatusIdSet;
        this.exclude = exclude;
    }

    @Override
    public void load(Kit<TwinEntity, UUID> twinsKit) {
        List<TwinFieldCalcProjection> calc = exclude ?
                twinFieldSimpleRepository.countChildrenTwinsWithStatusNotIn(twinsKit.getIdSet(), childrenTwinStatusIdSet) :
                twinFieldSimpleRepository.countChildrenTwinsWithStatusIn(twinsKit.getIdSet(), childrenTwinStatusIdSet);
        packResult(twinsKit, calc);
    }

    @Override
    boolean canBeMerged(Object o) {
        return isSameClass(o)
                && Objects.equals(this.twinClassFieldId, ((TwinFieldStorageCalcChildrenInStatusCount) o).twinClassFieldId)
                && Objects.equals(this.childrenTwinStatusIdSet, ((TwinFieldStorageCalcChildrenInStatusCount) o).childrenTwinStatusIdSet)
                && Objects.equals(this.exclude, ((TwinFieldStorageCalcChildrenInStatusCount) o).exclude);
    }
}
