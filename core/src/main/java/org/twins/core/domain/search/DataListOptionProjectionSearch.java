package org.twins.core.domain.search;

import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import org.cambium.common.util.SetUtils;
import org.twins.core.dao.datalist.DataListOptionProjectionEntity;
import org.twins.core.domain.DataTimeRange;

import java.util.Set;
import java.util.UUID;

@Data
@Accessors(chain = true)
@FieldNameConstants
public class DataListOptionProjectionSearch extends EntitySearch<DataListOptionProjectionEntity> {
    private Set<UUID> idList;
    private Set<UUID> idExcludeList;

    private Set<UUID> projectionTypeIdList;
    private Set<UUID> projectionTypeIdExcludeList;

    private Set<UUID> srcDataListOptionIdList;
    private Set<UUID> srcDataListOptionIdExcludeList;

    private Set<UUID> dstDataListOptionIdList;
    private Set<UUID> dstDataListOptionIdExcludeList;

    private Set<UUID> savedByUserIdList;
    private Set<UUID> savedByUserIdExcludeList;

    private DataTimeRange changedAt;

    public DataListOptionProjectionSearch addProjectionTypeId(UUID ProjectionTypeId, boolean exclude) {
        return SetUtils.safeAdd(this, ProjectionTypeId, exclude,
                this::getProjectionTypeIdList, this::setProjectionTypeIdList,
                this::getProjectionTypeIdExcludeList, this::setProjectionTypeIdExcludeList);
    }

    public DataListOptionProjectionSearch addSrcDataListOptionId(UUID optionId, boolean exclude) {
        return SetUtils.safeAdd(this, optionId, exclude,
                this::getSrcDataListOptionIdList, this::setSrcDataListOptionIdList,
                this::getSrcDataListOptionIdExcludeList, this::setSrcDataListOptionIdExcludeList);
    }

    public DataListOptionProjectionSearch addDstDataListOptionId(UUID optionId, boolean exclude) {
        return SetUtils.safeAdd(this, optionId, exclude,
                this::getDstDataListOptionIdList, this::setDstDataListOptionIdList,
                this::getDstDataListOptionIdExcludeList, this::setDstDataListOptionIdExcludeList);
    }

    public DataListOptionProjectionSearch addSavedByUserId(UUID userId, boolean exclude) {
        return SetUtils.safeAdd(this, userId, exclude,
                this::getSavedByUserIdList, this::setSavedByUserIdList,
                this::getSavedByUserIdExcludeList, this::setSavedByUserIdExcludeList);
    }
}
