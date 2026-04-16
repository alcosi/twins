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

public class TwinFieldStorageCalcSumOfSubtractionsByLink extends TwinFieldStorageCalc {

    private final TwinFieldDecimalRepository twinFieldDecimalRepository;
    private final UUID firstFieldId;
    private final UUID secondFieldId;
    private final UUID linkId;
    private final boolean srcElseDst;
    private final Set<UUID> linkedTwinInStatusIdList;
    private final Set<UUID> linkedTwinOfClassIds;
    private final boolean statusExclude;

    public TwinFieldStorageCalcSumOfSubtractionsByLink(UUID twinClassFieldId, TwinFieldDecimalRepository twinFieldDecimalRepository, UUID firstFieldId, UUID secondFieldId, UUID linkId, boolean srcElseDst, Set<UUID> linkedTwinInStatusIdList, Set<UUID> linkedTwinOfClassIds, boolean statusExclude) {
        super(twinClassFieldId);
        this.twinFieldDecimalRepository = twinFieldDecimalRepository;
        this.firstFieldId = firstFieldId;
        this.secondFieldId = secondFieldId;
        this.linkId = linkId;
        this.srcElseDst = srcElseDst;
        this.linkedTwinInStatusIdList = linkedTwinInStatusIdList;
        this.linkedTwinOfClassIds = linkedTwinOfClassIds;
        this.statusExclude = statusExclude;
    }

    @Override
    public void load(Kit<TwinEntity, UUID> twinsKit) throws ServiceException {
        List<TwinFieldCalcProjection> calc = twinFieldDecimalRepository.sumLinkedTwinFieldValuesOfSubtractionsByLink(
                twinsKit.getIdSet(),
                srcElseDst,
                linkedTwinInStatusIdList,
                linkedTwinOfClassIds,
                firstFieldId,
                secondFieldId,
                statusExclude
        );

        packResult(twinsKit, calc);
    }


    @Override
    public boolean canBeMerged(Object o) {
        return isSameClass(o)
                && Objects.equals(this.twinClassFieldId, ((TwinFieldStorageCalcSumOfSubtractionsByLink) o).twinClassFieldId)
                && Objects.equals(this.firstFieldId, ((TwinFieldStorageCalcSumOfSubtractionsByLink) o).firstFieldId)
                && Objects.equals(this.secondFieldId, ((TwinFieldStorageCalcSumOfSubtractionsByLink) o).secondFieldId)
                && Objects.equals(this.linkId, ((TwinFieldStorageCalcSumOfSubtractionsByLink) o).linkId)
                && Objects.equals(this.srcElseDst, ((TwinFieldStorageCalcSumOfSubtractionsByLink) o).srcElseDst)
                && Objects.equals(this.linkedTwinInStatusIdList, ((TwinFieldStorageCalcSumOfSubtractionsByLink) o).linkedTwinInStatusIdList)
                && Objects.equals(this.linkedTwinOfClassIds, ((TwinFieldStorageCalcSumOfSubtractionsByLink) o).linkedTwinOfClassIds)
                && Objects.equals(this.statusExclude, ((TwinFieldStorageCalcSumOfSubtractionsByLink) o).statusExclude);
    }
}
