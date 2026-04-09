package org.twins.core.featurer.fieldtyper.storage;

import org.cambium.common.kit.Kit;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinFieldCalcProjection;
import org.twins.core.dao.twin.TwinFieldSimpleRepository;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class TwinFieldStorageCalcChildrenOfClassCount extends TwinFieldStorageCalc {
    private final TwinFieldSimpleRepository twinFieldSimpleRepository;
    private final String lquery;

    public TwinFieldStorageCalcChildrenOfClassCount(
            TwinFieldSimpleRepository twinFieldSimpleRepository,
            UUID twinClassFieldId,
            String lquery) {
        super(twinClassFieldId);
        this.twinFieldSimpleRepository = twinFieldSimpleRepository;
        this.lquery = lquery;
    }

    @Override
    public void load(Kit<TwinEntity, UUID> twinsKit) {
        List<TwinFieldCalcProjection> calc =
                twinFieldSimpleRepository.countChildrenTwinsByExtendsHierarchy(twinsKit.getIdSet(), lquery);
        packResult(twinsKit, calc);
    }

    @Override
    boolean canBeMerged(Object o) {
        return isSameClass(o)
                && Objects.equals(this.twinClassFieldId, ((TwinFieldStorageCalcChildrenOfClassCount) o).twinClassFieldId)
                && Objects.equals(this.lquery, ((TwinFieldStorageCalcChildrenOfClassCount) o).lquery);
    }
}
