package org.twins.core.featurer.fieldtyper.storage;

import org.cambium.common.kit.Kit;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinFieldCalcProjection;
import org.twins.core.dao.twin.TwinFieldSimpleRepository;

import java.util.*;

public class TwinFieldStorageCalcSumByHead extends TwinFieldStorageCalc {
    private final TwinFieldSimpleRepository twinFieldSimpleRepository;
    private final Set<UUID> childrenTwinClassFieldIds;
    private final Set<UUID> childrenTwinStatusIdSet;
    private final Set<UUID> childrenTwinOfClassIdSet;
    private final boolean exclude;

    public TwinFieldStorageCalcSumByHead(TwinFieldSimpleRepository twinFieldSimpleRepository, UUID twinClassFieldId, Set<UUID> childrenTwinClassFieldIds, Set<UUID> childrenTwinStatusIdSet, Set<UUID> childrenTwinOfClassIdSet, boolean exclude) {
        super(twinClassFieldId);
        this.twinFieldSimpleRepository = twinFieldSimpleRepository;
        this.childrenTwinClassFieldIds = childrenTwinClassFieldIds;
        this.childrenTwinStatusIdSet = childrenTwinStatusIdSet == null ? Collections.emptySet() : childrenTwinStatusIdSet;
        this.childrenTwinOfClassIdSet = childrenTwinOfClassIdSet == null ? Collections.emptySet() : childrenTwinOfClassIdSet;
        this.exclude = exclude;
    }

    @Override
    public void load(Kit<TwinEntity, UUID> twinsKit) {
        List<TwinFieldCalcProjection> calc = twinFieldSimpleRepository.sumChildrenTwinFieldValuesByHead(
                twinsKit.getIdSet(),
                childrenTwinClassFieldIds,
                childrenTwinStatusIdSet,
                exclude,
                childrenTwinOfClassIdSet);

        packResult(twinsKit, calc);
    }

    @Override
    boolean canBeMerged(Object o) {
        return isSameClass(o)
                && Objects.equals(this.twinClassFieldId, ((TwinFieldStorageCalcSumByHead) o).twinClassFieldId)
                && Objects.equals(this.childrenTwinClassFieldIds, ((TwinFieldStorageCalcSumByHead) o).childrenTwinClassFieldIds)
                && Objects.equals(this.childrenTwinStatusIdSet, ((TwinFieldStorageCalcSumByHead) o).childrenTwinStatusIdSet)
                && Objects.equals(this.childrenTwinOfClassIdSet, ((TwinFieldStorageCalcSumByHead) o).childrenTwinOfClassIdSet)
                && Objects.equals(this.exclude, ((TwinFieldStorageCalcSumByHead) o).exclude);
    }
}
