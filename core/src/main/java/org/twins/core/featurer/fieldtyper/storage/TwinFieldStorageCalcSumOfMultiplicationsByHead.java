package org.twins.core.featurer.fieldtyper.storage;

import org.cambium.common.exception.ServiceException;
import org.cambium.common.kit.Kit;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinFieldCalcProjection;
import org.twins.core.dao.twin.TwinFieldDecimalRepository;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public class TwinFieldStorageCalcSumOfMultiplicationsByHead extends TwinFieldStorageCalc {

    private final TwinFieldDecimalRepository twinFieldDecimalRepository;
    private final UUID firstFieldId;
    private final UUID secondFieldId;
    private final Set<UUID> childrenTwinStatusIdSet;
    private final Set<UUID> childrenTwinOfClassIdSet;
    private final boolean exclude;

    public TwinFieldStorageCalcSumOfMultiplicationsByHead(UUID twinClassFieldId, TwinFieldDecimalRepository twinFieldDecimalRepository, UUID firstFieldId, UUID secondFieldId, Set<UUID> childrenTwinStatusIdSet, Set<UUID> childrenTwinOfClassIdSet, boolean exclude) {
        super(twinClassFieldId);
        this.twinFieldDecimalRepository = twinFieldDecimalRepository;
        this.firstFieldId = firstFieldId;
        this.secondFieldId = secondFieldId;
        this.childrenTwinStatusIdSet = childrenTwinStatusIdSet;
        this.childrenTwinOfClassIdSet = childrenTwinOfClassIdSet;
        this.exclude = exclude;
    }

    @Override
    public void load(Kit<TwinEntity, UUID> twinsKit) throws ServiceException {
        List<TwinFieldCalcProjection> calc = twinFieldDecimalRepository.sumChildrenTwinFieldValuesOfMultiplicationsByHead(
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
                && Objects.equals(this.twinClassFieldId, ((TwinFieldStorageCalcSumOfMultiplicationsByHead) o).twinClassFieldId)
                && Objects.equals(this.firstFieldId, ((TwinFieldStorageCalcSumOfMultiplicationsByHead) o).firstFieldId)
                && Objects.equals(this.secondFieldId, ((TwinFieldStorageCalcSumOfMultiplicationsByHead) o).secondFieldId)
                && Objects.equals(this.childrenTwinStatusIdSet, ((TwinFieldStorageCalcSumOfMultiplicationsByHead) o).childrenTwinStatusIdSet)
                && Objects.equals(this.childrenTwinOfClassIdSet, ((TwinFieldStorageCalcSumOfMultiplicationsByHead) o).childrenTwinOfClassIdSet)
                && Objects.equals(this.exclude, ((TwinFieldStorageCalcSumOfMultiplicationsByHead) o).exclude);
    }
}
