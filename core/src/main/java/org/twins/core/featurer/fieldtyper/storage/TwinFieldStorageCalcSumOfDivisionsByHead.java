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

public class TwinFieldStorageCalcSumOfDivisionsByHead extends TwinFieldStorageCalc {

    private final TwinFieldDecimalRepository twinFieldDecimalRepository;
    private final UUID firstFieldId;
    private final UUID secondFieldId;
    private final Set<UUID> childrenTwinStatusIdSet;
    private final Set<UUID> childrenTwinOfClassIdSet;
    private final boolean exclude;
    private final boolean divisionByZeroIgnore;

    public TwinFieldStorageCalcSumOfDivisionsByHead(UUID twinClassFieldId, TwinFieldDecimalRepository twinFieldDecimalRepository, UUID firstFieldId, UUID secondFieldId, Set<UUID> childrenTwinStatusIdSet, Set<UUID> childrenTwinOfClassIdSet, boolean exclude, boolean divisionByZeroIgnore) {
        super(twinClassFieldId);
        this.twinFieldDecimalRepository = twinFieldDecimalRepository;
        this.firstFieldId = firstFieldId;
        this.secondFieldId = secondFieldId;
        this.childrenTwinStatusIdSet = childrenTwinStatusIdSet;
        this.childrenTwinOfClassIdSet = childrenTwinOfClassIdSet;
        this.exclude = exclude;
        this.divisionByZeroIgnore = divisionByZeroIgnore;
    }

    @Override
    public void load(Kit<TwinEntity, UUID> twinsKit) throws ServiceException {
        List<TwinFieldCalcProjection> calc = twinFieldDecimalRepository.sumChildrenTwinFieldValuesOfDivisionsByHead(
                twinsKit.getIdSet(),
                childrenTwinStatusIdSet,
                childrenTwinOfClassIdSet,
                firstFieldId,
                secondFieldId,
                exclude,
                divisionByZeroIgnore
        );

        packResult(twinsKit, calc);
    }


    @Override
    boolean canBeMerged(Object o) {
        return isSameClass(o)
                && Objects.equals(this.twinClassFieldId, ((TwinFieldStorageCalcSumOfDivisionsByHead) o).twinClassFieldId)
                && Objects.equals(this.firstFieldId, ((TwinFieldStorageCalcSumOfDivisionsByHead) o).firstFieldId)
                && Objects.equals(this.secondFieldId, ((TwinFieldStorageCalcSumOfDivisionsByHead) o).secondFieldId)
                && Objects.equals(this.childrenTwinStatusIdSet, ((TwinFieldStorageCalcSumOfDivisionsByHead) o).childrenTwinStatusIdSet)
                && Objects.equals(this.childrenTwinOfClassIdSet, ((TwinFieldStorageCalcSumOfDivisionsByHead) o).childrenTwinOfClassIdSet)
                && Objects.equals(this.exclude, ((TwinFieldStorageCalcSumOfDivisionsByHead) o).exclude);
    }
}
