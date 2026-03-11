package org.twins.core.featurer.fieldtyper.storage;

import org.cambium.common.kit.Kit;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinFieldCalcProjection;
import org.twins.core.dao.twin.TwinFieldDecimalRepository;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;


public class TwinFieldStorageCalcSumByHead extends TwinFieldStorageCalc {

    private final TwinFieldDecimalRepository twinFieldDecimalRepository;
    private final Set<UUID> childrenTwinClassFieldIds;
    private final Set<UUID> childrenTwinStatusIdSet;
    private final Set<UUID> childrenTwinOfClassIdSet;
    private final boolean exclude;

    public TwinFieldStorageCalcSumByHead(TwinFieldDecimalRepository twinFieldDecimalRepository, UUID twinClassFieldId, Set<UUID> childrenTwinClassFieldIds, Set<UUID> childrenTwinStatusIdSet, Set<UUID> childrenTwinOfClassIdSet, boolean exclude) {
        super(twinClassFieldId);
        this.twinFieldDecimalRepository = twinFieldDecimalRepository;
        this.childrenTwinClassFieldIds = childrenTwinClassFieldIds;
        this.childrenTwinStatusIdSet = childrenTwinStatusIdSet;
        this.childrenTwinOfClassIdSet = childrenTwinOfClassIdSet;
        this.exclude = exclude;
    }

    @Override
    public void load(Kit<TwinEntity, UUID> twinsKit) {
        List<TwinFieldCalcProjection> calc = twinFieldDecimalRepository.sumChildrenTwinFieldValuesByHead(
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
