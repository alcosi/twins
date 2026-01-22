package org.twins.core.featurer.fieldtyper.storage;

import org.cambium.common.exception.ServiceException;
import org.cambium.common.kit.Kit;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinFieldCalcProjection;
import org.twins.core.dao.twin.TwinFieldSimpleRepository;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public class TwinFieldStorageCalcSumOfSubtractionsByHead extends TwinFieldStorageCalc {
    private final TwinFieldSimpleRepository twinFieldSimpleRepository;
    private final UUID firstFieldId;
    private final UUID secondFieldId;
    private final Set<UUID> childrenTwinStatusIdSet;
    private final Set<UUID> childrenTwinOfClassIdSet;
    private final boolean exclude;

    public TwinFieldStorageCalcSumOfSubtractionsByHead(UUID twinClassFieldId, TwinFieldSimpleRepository twinFieldSimpleRepository, UUID firstFieldId, UUID secondFieldId, Set<UUID> childrenTwinStatusIdSet, Set<UUID> childrenTwinOfClassIdSet, boolean exclude) {
        super(twinClassFieldId);
        this.twinFieldSimpleRepository = twinFieldSimpleRepository;
        this.firstFieldId = firstFieldId;
        this.secondFieldId = secondFieldId;
        this.childrenTwinStatusIdSet = childrenTwinStatusIdSet;
        this.childrenTwinOfClassIdSet = childrenTwinOfClassIdSet;
        this.exclude = exclude;
    }

    @Override
    public void load(Kit<TwinEntity, UUID> twinsKit) throws ServiceException {
        List<TwinFieldCalcProjection> calc = twinFieldSimpleRepository.sumChildrenTwinFieldValuesOfSubtractionsByHead(
                twinsKit.getIdSet(),
                childrenTwinStatusIdSet,
                childrenTwinOfClassIdSet,
                firstFieldId,
                secondFieldId,
                exclude
        );

        packResult(twinsKit, calc);
    }


    @Override
    boolean canBeMerged(Object o) {
        return isSameClass(o)
                && Objects.equals(this.twinClassFieldId, ((TwinFieldStorageCalcSumOfSubtractionsByHead) o).twinClassFieldId)
                && Objects.equals(this.firstFieldId, ((TwinFieldStorageCalcSumOfSubtractionsByHead) o).firstFieldId)
                && Objects.equals(this.secondFieldId, ((TwinFieldStorageCalcSumOfSubtractionsByHead) o).secondFieldId)
                && Objects.equals(this.childrenTwinStatusIdSet, ((TwinFieldStorageCalcSumOfSubtractionsByHead) o).childrenTwinStatusIdSet)
                && Objects.equals(this.childrenTwinOfClassIdSet, ((TwinFieldStorageCalcSumOfSubtractionsByHead) o).childrenTwinOfClassIdSet)
                && Objects.equals(this.exclude, ((TwinFieldStorageCalcSumOfSubtractionsByHead) o).exclude);
    }
}
