package org.twins.core.domain.search;

import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import org.cambium.common.util.CollectionUtils;

import java.util.Set;
import java.util.UUID;

@Data
@Accessors(chain = true)
@FieldNameConstants
public class DataListOptionProjectionSearch {
    private Set<UUID> idList;
    private Set<UUID> idExcludeList;

    private Set<UUID> dataListProjectionIdList;
    private Set<UUID> dataListProjectionIdExcludeList;

    private Set<UUID> srcDataListOptionIdList;
    private Set<UUID> srcDataListOptionIdExcludeList;

    private Set<UUID> dstDataListOptionIdList;
    private Set<UUID> dstDataListOptionIdExcludeList;

    private Set<UUID> savedByUserIdList;
    private Set<UUID> savedByUserIdExcludeList;

    public DataListOptionProjectionSearch addDataListProjectionId(UUID dataListProjectionId, boolean exclude) {
        if (exclude)
            dataListProjectionIdExcludeList = CollectionUtils.safeAdd(dataListProjectionIdExcludeList, dataListProjectionId);
        else
            dataListProjectionIdList = CollectionUtils.safeAdd(dataListProjectionIdList, dataListProjectionId);
        return this;
    }

    public DataListOptionProjectionSearch addSrcDataListOptionId(UUID optionId, boolean exclude) {
        if (exclude)
            srcDataListOptionIdExcludeList = CollectionUtils.safeAdd(srcDataListOptionIdExcludeList, optionId);
        else
            srcDataListOptionIdList = CollectionUtils.safeAdd(srcDataListOptionIdList, optionId);
        return this;
    }

    public DataListOptionProjectionSearch addDstDataListOptionId(UUID optionId, boolean exclude) {
        if (exclude)
            dstDataListOptionIdExcludeList = CollectionUtils.safeAdd(dstDataListOptionIdExcludeList, optionId);
        else
            dstDataListOptionIdList = CollectionUtils.safeAdd(dstDataListOptionIdList, optionId);
        return this;
    }

    public DataListOptionProjectionSearch addSavedByUserId(UUID userId, boolean exclude) {
        if (exclude)
            savedByUserIdExcludeList = CollectionUtils.safeAdd(savedByUserIdExcludeList, userId);
        else
            savedByUserIdList = CollectionUtils.safeAdd(savedByUserIdList, userId);
        return this;
    }
}
