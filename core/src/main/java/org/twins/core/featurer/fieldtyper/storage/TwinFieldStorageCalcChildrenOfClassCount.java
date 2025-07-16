package org.twins.core.featurer.fieldtyper.storage;

import org.cambium.common.kit.Kit;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinFieldCalcProjection;
import org.twins.core.dao.twin.TwinFieldSimpleRepository;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public class TwinFieldStorageCalcChildrenOfClassCount extends TwinFieldStorageCalc {
    private final TwinFieldSimpleRepository twinFieldSimpleRepository;
    private final Set<UUID> childrenTwinClassIdSet;

    public TwinFieldStorageCalcChildrenOfClassCount(TwinFieldSimpleRepository twinFieldSimpleRepository, UUID twinClassFieldId, Set<UUID> childrenTwinClassIdSet) {
        super(twinClassFieldId);
        this.twinFieldSimpleRepository = twinFieldSimpleRepository;
        this.childrenTwinClassIdSet = childrenTwinClassIdSet;
    }

    @Override
    public void load(Kit<TwinEntity, UUID> twinsKit) {
        List<TwinFieldCalcProjection> calc =
                twinFieldSimpleRepository.countChildrenTwinsOfTwinClassIdIn(twinsKit.getIdSet(), childrenTwinClassIdSet);
        packResult(twinsKit, calc);
    }

    @Override
    boolean canBeMerged(Object o) {
        return isSameClass(o)
                && Objects.equals(this.twinClassFieldId, ((TwinFieldStorageCalcChildrenOfClassCount) o).twinClassFieldId)
                && Objects.equals(this.childrenTwinClassIdSet, ((TwinFieldStorageCalcChildrenOfClassCount) o).childrenTwinClassIdSet);
    }
}
