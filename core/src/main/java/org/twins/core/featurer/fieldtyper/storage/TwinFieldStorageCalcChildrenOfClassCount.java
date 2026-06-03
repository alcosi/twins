package org.twins.core.featurer.fieldtyper.storage;

import org.cambium.common.kit.Kit;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinFieldCalcProjection;
import org.twins.core.dao.twin.TwinFieldSimpleRepository;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public class TwinFieldStorageCalcChildrenOfClassCount extends TwinFieldStorageCalc {
    private final TwinFieldSimpleRepository twinFieldSimpleRepository;
    private final String lquery;
    private final Set<UUID> classIds;
    private final boolean useExtendsHierarchy;

    public TwinFieldStorageCalcChildrenOfClassCount(
            TwinFieldSimpleRepository twinFieldSimpleRepository,
            UUID twinClassFieldId,
            String lquery,
            UUID calcUserId,
            UUID calcUserGroupFootprintId) {
        this(twinFieldSimpleRepository, twinClassFieldId, lquery, null, true, calcUserId, calcUserGroupFootprintId);
    }

    public TwinFieldStorageCalcChildrenOfClassCount(
            TwinFieldSimpleRepository twinFieldSimpleRepository,
            UUID twinClassFieldId,
            Set<UUID> classIds,
            UUID calcUserId,
            UUID calcUserGroupFootprintId) {
        this(twinFieldSimpleRepository, twinClassFieldId, null, classIds, false, calcUserId, calcUserGroupFootprintId);
    }

    private TwinFieldStorageCalcChildrenOfClassCount(
            TwinFieldSimpleRepository twinFieldSimpleRepository,
            UUID twinClassFieldId,
            String lquery,
            Set<UUID> classIds,
            boolean useExtendsHierarchy,
            UUID calcUserId,
            UUID calcUserGroupFootprintId) {
        super(twinClassFieldId, calcUserId, calcUserGroupFootprintId);
        this.twinFieldSimpleRepository = twinFieldSimpleRepository;
        this.lquery = lquery;
        this.classIds = classIds;
        this.useExtendsHierarchy = useExtendsHierarchy;
    }

    @Override
    public void load(Kit<TwinEntity, UUID> twinsKit) {
        List<TwinFieldCalcProjection> calc;
        if (useExtendsHierarchy) {
            calc = twinFieldSimpleRepository.countChildrenTwinsByExtendsHierarchy(twinsKit.getIdSet(), lquery, calcUserId, calcUserGroupFootprintId);
        } else {
            calc = twinFieldSimpleRepository.countChildrenTwinsOfTwinClassIdIn(twinsKit.getIdSet(), classIds, calcUserId, calcUserGroupFootprintId);
        }
        packResult(twinsKit, calc);
    }

    @Override
    boolean canBeMerged(Object o) {
        if (!isSameClass(o) || !Objects.equals(this.twinClassFieldId, ((TwinFieldStorageCalcChildrenOfClassCount) o).twinClassFieldId)) {
            return false;
        }
        if (!hasSameCalcPermissionContext((TwinFieldStorageCalcChildrenOfClassCount) o)) {
            return false;
        }
        TwinFieldStorageCalcChildrenOfClassCount other = (TwinFieldStorageCalcChildrenOfClassCount) o;
        if (this.useExtendsHierarchy != other.useExtendsHierarchy) {
            return false;
        }
        return useExtendsHierarchy
                ? Objects.equals(this.lquery, other.lquery)
                : Objects.equals(this.classIds, other.classIds);
    }
}
