package org.twins.core.domain.search;

import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import org.cambium.common.util.CollectionUtils;
import org.twins.core.dao.datalist.DataListOptionEntity;

import java.util.Set;
import java.util.UUID;

@Data
@Accessors(chain = true)
@FieldNameConstants
public class DataListOptionSearch {
    private Set<UUID> idList;
    private Set<UUID> idExcludeList;
    private Set<UUID> dataListIdList;
    private Set<UUID> dataListIdExcludeList;
    private Set<String> dataListKeyList;
    private Set<String> dataListKeyExcludeList;
    private Set<String> optionLikeList;
    private Set<String> optionNotLikeList;
    private Set<String> optionI18nLikeList;
    private Set<String> optionI18nNotLikeList;
    private Set<String> externalIdLikeList;
    private Set<String> externalIdNotLikeList;
    private Set<UUID> businessAccountIdList;
    private Set<UUID> businessAccountIdExcludeList;
    private Set<UUID> dataListSubsetIdList;
    private Set<UUID> dataListSubsetIdExcludeList;
    private Set<String> dataListSubsetKeyList;
    private Set<String> dataListSubsetKeyExcludeList;
    private Set<DataListOptionEntity.Status> statusIdList;
    private Set<DataListOptionEntity.Status> statusIdExcludeList;

    public DataListOptionSearch addDataListId(UUID datalistId, boolean exclude) {
        if (exclude)
            dataListIdExcludeList = CollectionUtils.safeAdd(dataListIdExcludeList, datalistId);
        else
            dataListIdList = CollectionUtils.safeAdd(dataListIdList, datalistId);
        return this;
    }

    public DataListOptionSearch addBusinessAccountId(UUID businessAccountId, boolean exclude) {
        if (exclude)
            businessAccountIdExcludeList = CollectionUtils.safeAdd(businessAccountIdExcludeList, businessAccountId);
        else
            businessAccountIdList = CollectionUtils.safeAdd(businessAccountIdList, businessAccountId);
        return this;
    }

    public DataListOptionSearch addExternalId(String externalId, boolean exclude) {
        if (exclude)
            externalIdNotLikeList = CollectionUtils.safeAdd(externalIdNotLikeList, externalId);
        else
            externalIdLikeList = CollectionUtils.safeAdd(externalIdLikeList, externalId);
        return this;
    }
}
