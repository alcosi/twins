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

public class TwinFieldStorageCalcSumChildrenOfLinked extends TwinFieldStorageCalc {

    private final TwinFieldDecimalRepository twinFieldDecimalRepository;
    private final Set<UUID> fieldIds;
    private final Set<UUID> linkIds;
    private final boolean srcElseDst;
    private final Set<UUID> linkedTwinInStatusIdSet;
    private final Set<UUID> linkedTwinOfClassIds;
    private final boolean linkedStatusExclude;
    private final Set<UUID> childrenTwinOfClassIds;
    private final Set<UUID> childrenTwinInStatusIds;
    private final boolean childrenStatusExclude;

    public TwinFieldStorageCalcSumChildrenOfLinked(
            UUID twinClassFieldId,
            TwinFieldDecimalRepository twinFieldDecimalRepository,
            Set<UUID> fieldIds,
            Set<UUID> linkIds,
            boolean srcElseDst,
            Set<UUID> linkedTwinInStatusIdSet,
            Set<UUID> linkedTwinOfClassIds,
            boolean linkedStatusExclude,
            Set<UUID> childrenTwinOfClassIds,
            Set<UUID> childrenTwinInStatusIds,
            boolean childrenStatusExclude
    ) {
        super(twinClassFieldId);
        this.twinFieldDecimalRepository = twinFieldDecimalRepository;
        this.fieldIds = fieldIds;
        this.linkIds = linkIds;
        this.srcElseDst = srcElseDst;
        this.linkedTwinInStatusIdSet = linkedTwinInStatusIdSet;
        this.linkedTwinOfClassIds = linkedTwinOfClassIds;
        this.linkedStatusExclude = linkedStatusExclude;
        this.childrenTwinOfClassIds = childrenTwinOfClassIds;
        this.childrenTwinInStatusIds = childrenTwinInStatusIds;
        this.childrenStatusExclude = childrenStatusExclude;
    }

    @Override
    public void load(Kit<TwinEntity, UUID> twinsKit) throws ServiceException {
        List<TwinFieldCalcProjection> calc = twinFieldDecimalRepository.sumChildrenFieldsOfLinkedTwins(
                twinsKit.getIdSet(),
                srcElseDst,
                linkedTwinInStatusIdSet,
                linkedTwinOfClassIds,
                linkedStatusExclude,
                linkIds,
                childrenTwinOfClassIds,
                childrenTwinInStatusIds,
                childrenStatusExclude,
                fieldIds
        );
        packResult(twinsKit, calc);
    }

    @Override
    boolean canBeMerged(Object o) {
        if (!isSameClass(o)) {
            return false;
        }
        TwinFieldStorageCalcSumChildrenOfLinked other = (TwinFieldStorageCalcSumChildrenOfLinked) o;
        return Objects.equals(this.twinClassFieldId, other.twinClassFieldId)
                && Objects.equals(this.fieldIds, other.fieldIds)
                && Objects.equals(this.linkIds, other.linkIds)
                && Objects.equals(this.srcElseDst, other.srcElseDst)
                && Objects.equals(this.linkedTwinInStatusIdSet, other.linkedTwinInStatusIdSet)
                && Objects.equals(this.linkedTwinOfClassIds, other.linkedTwinOfClassIds)
                && Objects.equals(this.linkedStatusExclude, other.linkedStatusExclude)
                && Objects.equals(this.childrenTwinOfClassIds, other.childrenTwinOfClassIds)
                && Objects.equals(this.childrenTwinInStatusIds, other.childrenTwinInStatusIds)
                && Objects.equals(this.childrenStatusExclude, other.childrenStatusExclude);
    }
}
