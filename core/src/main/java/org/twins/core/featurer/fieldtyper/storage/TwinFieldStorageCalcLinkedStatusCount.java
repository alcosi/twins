package org.twins.core.featurer.fieldtyper.storage;

import org.cambium.common.kit.Kit;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinFieldCalcProjection;
import org.twins.core.dao.twin.TwinRepository;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public class TwinFieldStorageCalcLinkedStatusCount extends TwinFieldStorageCalc {
    private final TwinRepository twinRepository;
    private final Set<UUID> childrenTwinStatusIdSet;
    private final boolean exclude;

    public TwinFieldStorageCalcLinkedStatusCount(TwinRepository twinRepository, UUID twinClassFieldId, Set<UUID> childrenTwinStatusIdSet, boolean exclude) {
        super(twinClassFieldId);
        this.twinRepository = twinRepository;
        this.childrenTwinStatusIdSet = childrenTwinStatusIdSet;
        this.exclude = exclude;
    }

    @Override
    public void load(Kit<TwinEntity, UUID> twinsKit) {
        List<TwinFieldCalcProjection> calc = exclude ?
                twinRepository.countChildrenTwinsWithStatusNotIn(twinsKit.getIdSet(), childrenTwinStatusIdSet) :
                twinRepository.countChildrenTwinsWithStatusIn(twinsKit.getIdSet(), childrenTwinStatusIdSet);
        packResult(twinsKit, calc);
    }

    @Override
    boolean canBeMerged(Object o) {
        return isSameClass(o)
                && Objects.equals(this.twinClassFieldId, ((TwinFieldStorageCalcLinkedStatusCount) o).twinClassFieldId)
                && Objects.equals(this.childrenTwinStatusIdSet, ((TwinFieldStorageCalcLinkedStatusCount) o).childrenTwinStatusIdSet)
                && Objects.equals(this.exclude, ((TwinFieldStorageCalcLinkedStatusCount) o).exclude);
    }
}
