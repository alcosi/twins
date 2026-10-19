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

public class TwinFieldStorageCalcSumOfMultiplicationsByLink extends TwinFieldStorageCalc {

    private final TwinFieldDecimalRepository twinFieldDecimalRepository;
    private final UUID firstFieldId;
    private final UUID secondFieldId;
    private final Set<UUID> linkIds;
    private final boolean srcElseDst;
    private final Set<UUID> linkedTwinInStatusIdList;
    private final Set<UUID> linkedTwinOfClassIds;
    private final boolean statusExclude;

    public TwinFieldStorageCalcSumOfMultiplicationsByLink(UUID twinClassFieldId, TwinFieldDecimalRepository twinFieldDecimalRepository, UUID firstFieldId, UUID secondFieldId, Set<UUID> linkIds, boolean srcElseDst, Set<UUID> linkedTwinInStatusIdList, Set<UUID> linkedTwinOfClassIds, boolean statusExclude) {
        super(twinClassFieldId);
        this.twinFieldDecimalRepository = twinFieldDecimalRepository;
        this.firstFieldId = firstFieldId;
        this.secondFieldId = secondFieldId;
        this.linkIds = linkIds;
        this.srcElseDst = srcElseDst;
        this.linkedTwinInStatusIdList = linkedTwinInStatusIdList;
        this.linkedTwinOfClassIds = linkedTwinOfClassIds;
        this.statusExclude = statusExclude;
    }

    @Override
    public void load(Kit<TwinEntity, UUID> twinsKit) throws ServiceException {
        List<TwinFieldCalcProjection> calc = twinFieldDecimalRepository.sumLinkedTwinFieldValuesOfMultiplicationsByLink(
                twinsKit.getIdSet(),
                srcElseDst,
                linkedTwinInStatusIdList,
                linkedTwinOfClassIds,
                firstFieldId,
                secondFieldId,
                linkIds,
                statusExclude
        );

        packResult(twinsKit, calc);
    }


    @Override
    public boolean canBeMerged(Object o) {
        return isSameClass(o)
                && Objects.equals(this.twinClassFieldId, ((TwinFieldStorageCalcSumOfMultiplicationsByLink) o).twinClassFieldId)
                && Objects.equals(this.firstFieldId, ((TwinFieldStorageCalcSumOfMultiplicationsByLink) o).firstFieldId)
                && Objects.equals(this.secondFieldId, ((TwinFieldStorageCalcSumOfMultiplicationsByLink) o).secondFieldId)
                && Objects.equals(this.linkIds, ((TwinFieldStorageCalcSumOfMultiplicationsByLink) o).linkIds)
                && Objects.equals(this.srcElseDst, ((TwinFieldStorageCalcSumOfMultiplicationsByLink) o).srcElseDst)
                && Objects.equals(this.linkedTwinInStatusIdList, ((TwinFieldStorageCalcSumOfMultiplicationsByLink) o).linkedTwinInStatusIdList)
                && Objects.equals(this.linkedTwinOfClassIds, ((TwinFieldStorageCalcSumOfMultiplicationsByLink) o).linkedTwinOfClassIds)
                && Objects.equals(this.statusExclude, ((TwinFieldStorageCalcSumOfMultiplicationsByLink) o).statusExclude);
    }
}
