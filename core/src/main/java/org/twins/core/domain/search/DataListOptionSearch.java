package org.twins.core.domain.search;

import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import org.cambium.common.util.SetUtils;
import org.cambium.common.util.Ternary;
import org.twins.core.dao.datalist.DataListOptionEntity;
import org.twins.core.dao.datalist.DataListOptionSearchEntity;
import org.twins.core.enums.datalist.DataListStatus;

import java.util.Set;
import java.util.UUID;

@Data
@Accessors(chain = true)
@FieldNameConstants
public class DataListOptionSearch extends EntitySearch<DataListOptionEntity> implements ConfiguredSearchHolder<DataListOptionSearchEntity> {
    @SearchFilter
    private Set<UUID> idList;

    @SearchFilter
    private Set<UUID> idExcludeList;

    @SearchFilter
    private Set<UUID> dataListIdList;

    @SearchFilter
    private Set<UUID> dataListIdExcludeList;

    @SearchFilter
    private Set<String> dataListKeyList;

    @SearchFilter
    private Set<String> dataListKeyExcludeList;

    @SearchFilter
    private Set<String> optionLikeList;

    @SearchFilter
    private Set<String> optionNotLikeList;

    @SearchFilter
    private Set<String> optionI18nLikeList;

    @SearchFilter
    private Set<String> optionI18nNotLikeList;

    @SearchFilter
    private Set<String> externalIdList;

    @SearchFilter
    private Set<String> externalIdExcludeList;

    @SearchFilter
    private Set<UUID> businessAccountIdList;

    @SearchFilter
    private Set<UUID> businessAccountIdExcludeList;

    @SearchFilter
    private Set<UUID> dataListSubsetIdList;

    @SearchFilter
    private Set<UUID> dataListSubsetIdExcludeList;

    @SearchFilter
    private Set<String> dataListSubsetKeyList;

    @SearchFilter
    private Set<String> dataListSubsetKeyExcludeList;

    @SearchFilter
    private Set<DataListStatus> statusIdList;

    @SearchFilter
    private Set<DataListStatus> statusIdExcludeList;

    @SearchFilter
    private Set<UUID> validForTwinClassFieldIdList;

    @SearchFilter
    private Ternary custom;

    private DataListOptionSearchEntity configuredSearch;
    private boolean emptySearch = false;

    public DataListOptionSearch addDataListId(UUID datalistId, boolean exclude) {
        return SetUtils.safeAdd(this, datalistId, exclude,
                this::getDataListIdList, this::setDataListIdList,
                this::getDataListIdExcludeList, this::setDataListIdExcludeList);
    }

    public DataListOptionSearch addBusinessAccountId(UUID businessAccountId, boolean exclude) {
        return SetUtils.safeAdd(this, businessAccountId, exclude,
                this::getBusinessAccountIdList, this::setBusinessAccountIdList,
                this::getBusinessAccountIdExcludeList, this::setBusinessAccountIdExcludeList);
    }

    public DataListOptionSearch addExternalId(String externalId, boolean exclude) {
        return SetUtils.safeAdd(this, externalId, exclude,
                this::getExternalIdList, this::setExternalIdList,
                this::getExternalIdExcludeList, this::setExternalIdExcludeList);
    }

    public DataListOptionSearch addOptionKeyLike(String optionKey, boolean exclude) {
        return SetUtils.safeAdd(this, optionKey, exclude,
                this::getOptionLikeList, this::setOptionLikeList,
                this::getOptionNotLikeList, this::setOptionNotLikeList);
    }
}
