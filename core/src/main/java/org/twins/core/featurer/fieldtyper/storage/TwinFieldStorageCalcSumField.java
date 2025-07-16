package org.twins.core.featurer.fieldtyper.storage;

import org.cambium.common.kit.Kit;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinFieldCalcProjection;
import org.twins.core.dao.twin.TwinFieldSimpleRepository;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public class TwinFieldStorageCalcSumField extends TwinFieldStorageCalc {
    private final TwinFieldSimpleRepository twinFieldSimpleRepository;
    private final UUID childrenTwinClassFieldId;
    private final Set<UUID> childrenTwinStatusIdSet;
    private final boolean exclude;

    public TwinFieldStorageCalcSumField(TwinFieldSimpleRepository twinFieldSimpleRepository, UUID twinClassFieldId, UUID childrenTwinClassFieldId, Set<UUID> childrenTwinStatusIdSet, boolean exclude) {
        super(twinClassFieldId);
        this.twinFieldSimpleRepository = twinFieldSimpleRepository;
        this.childrenTwinClassFieldId = childrenTwinClassFieldId;
        this.childrenTwinStatusIdSet = childrenTwinStatusIdSet;
        this.exclude = exclude;
    }

    @Override
    public void load(Kit<TwinEntity, UUID> twinsKit) {
        List<TwinFieldCalcProjection> calc = exclude ?
                twinFieldSimpleRepository.sumChildrenTwinFieldValuesWithStatusNotIn(twinsKit.getIdSet(), childrenTwinClassFieldId, childrenTwinStatusIdSet) :
                twinFieldSimpleRepository.sumChildrenTwinFieldValuesWithStatusIn(twinsKit.getIdSet(), childrenTwinClassFieldId, childrenTwinStatusIdSet);
        packResult(twinsKit, calc);
    }

    @Override
    boolean canBeMerged(Object o) {
        return isSameClass(o)
                && Objects.equals(this.twinClassFieldId, ((TwinFieldStorageCalcSumField) o).twinClassFieldId)
                && Objects.equals(this.childrenTwinClassFieldId, ((TwinFieldStorageCalcSumField) o).childrenTwinClassFieldId)
                && Objects.equals(this.childrenTwinStatusIdSet, ((TwinFieldStorageCalcSumField) o).childrenTwinStatusIdSet)
                && Objects.equals(this.exclude, ((TwinFieldStorageCalcSumField) o).exclude);
    }
}
