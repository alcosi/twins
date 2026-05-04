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

public class TwinFieldStorageCalcSumOfDivisionsByLink extends TwinFieldStorageCalc {

    private final TwinFieldDecimalRepository twinFieldDecimalRepository;
    private final UUID firstFieldId;
    private final UUID secondFieldId;
    private final Set<UUID> linkIds;
    private final boolean srcElseDst;
    private final Set<UUID> linkedTwinInStatusIdList;
    private final Set<UUID> linkedTwinOfClassIds;
    private final boolean statusExclude;
    private final boolean divisionByZeroIgnore;

    public TwinFieldStorageCalcSumOfDivisionsByLink(UUID twinClassFieldId, TwinFieldDecimalRepository twinFieldDecimalRepository, UUID firstFieldId, UUID secondFieldId, Set<UUID> linkIds, boolean srcElseDst, Set<UUID> linkedTwinInStatusIdList, Set<UUID> linkedTwinOfClassIds, boolean statusExclude, boolean divisionByZeroIgnore) {
        super(twinClassFieldId);
        this.twinFieldDecimalRepository = twinFieldDecimalRepository;
        this.firstFieldId = firstFieldId;
        this.secondFieldId = secondFieldId;
        this.linkIds = linkIds;
        this.srcElseDst = srcElseDst;
        this.linkedTwinInStatusIdList = linkedTwinInStatusIdList;
        this.linkedTwinOfClassIds = linkedTwinOfClassIds;
        this.statusExclude = statusExclude;
        this.divisionByZeroIgnore = divisionByZeroIgnore;
    }


    @Override
    public void load(Kit<TwinEntity, UUID> twinsKit) throws ServiceException {
        List<TwinFieldCalcProjection> calc = twinFieldDecimalRepository.sumLinkedTwinFieldValuesOfDivisionsByLink(
                twinsKit.getIdSet(),
                srcElseDst,
                linkedTwinInStatusIdList,
                linkedTwinOfClassIds,
                firstFieldId,
                secondFieldId,
                linkIds,
                statusExclude,
                divisionByZeroIgnore
        );

        packResult(twinsKit, calc);
    }


    @Override
    public boolean canBeMerged(Object o) {
        return isSameClass(o)
                && Objects.equals(this.twinClassFieldId, ((TwinFieldStorageCalcSumOfDivisionsByLink) o).twinClassFieldId)
                && Objects.equals(this.firstFieldId, ((TwinFieldStorageCalcSumOfDivisionsByLink) o).firstFieldId)
                && Objects.equals(this.secondFieldId, ((TwinFieldStorageCalcSumOfDivisionsByLink) o).secondFieldId)
                && Objects.equals(this.linkIds, ((TwinFieldStorageCalcSumOfDivisionsByLink) o).linkIds)
                && Objects.equals(this.srcElseDst, ((TwinFieldStorageCalcSumOfDivisionsByLink) o).srcElseDst)
                && Objects.equals(this.linkedTwinInStatusIdList, ((TwinFieldStorageCalcSumOfDivisionsByLink) o).linkedTwinInStatusIdList)
                && Objects.equals(this.linkedTwinOfClassIds, ((TwinFieldStorageCalcSumOfDivisionsByLink) o).linkedTwinOfClassIds)
                && Objects.equals(this.statusExclude, ((TwinFieldStorageCalcSumOfDivisionsByLink) o).statusExclude);
    }
}
