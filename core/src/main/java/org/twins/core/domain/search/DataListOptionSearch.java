package org.twins.core.domain.search;

import com.google.common.collect.ImmutableList;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import org.apache.commons.lang3.tuple.Pair;
import org.cambium.common.util.CollectionUtils;
import org.cambium.common.util.Ternary;
import org.twins.core.dao.datalist.DataListOptionSearchEntity;
import org.twins.core.enums.datalist.DataListStatus;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;

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
    private Set<String> externalIdList;
    private Set<String> externalIdExcludeList;
    private Set<UUID> businessAccountIdList;
    private Set<UUID> businessAccountIdExcludeList;
    private Set<UUID> dataListSubsetIdList;
    private Set<UUID> dataListSubsetIdExcludeList;
    private Set<String> dataListSubsetKeyList;
    private Set<String> dataListSubsetKeyExcludeList;
    private Set<DataListStatus> statusIdList;
    private Set<DataListStatus> statusIdExcludeList;
    private Set<UUID> validForTwinClassFieldIdList;
    private Ternary custom;

    private DataListOptionSearchEntity configuredSearch;

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
            externalIdExcludeList = CollectionUtils.safeAdd(externalIdExcludeList, externalId);
        else
            externalIdList = CollectionUtils.safeAdd(externalIdList, externalId);
        return this;
    }

    public DataListOptionSearch addOptionKeyLike(String optionKey, boolean exclude) {
        if (exclude)
            optionNotLikeList = CollectionUtils.safeAdd(optionNotLikeList, optionKey);
        else
            optionLikeList = CollectionUtils.safeAdd(optionLikeList, optionKey);
        return this;
    }

    public static final ImmutableList<Pair<Function<DataListOptionSearch, Set>, BiConsumer<DataListOptionSearch, Set>>> SET_FIELDS = ImmutableList.of(
            Pair.of(DataListOptionSearch::getIdList, DataListOptionSearch::setIdList),
            Pair.of(DataListOptionSearch::getIdExcludeList, DataListOptionSearch::setIdExcludeList),
            Pair.of(DataListOptionSearch::getDataListIdList, DataListOptionSearch::setDataListIdList),
            Pair.of(DataListOptionSearch::getDataListIdExcludeList, DataListOptionSearch::setDataListIdExcludeList),
            Pair.of(DataListOptionSearch::getDataListKeyList, DataListOptionSearch::setDataListKeyList),
            Pair.of(DataListOptionSearch::getDataListKeyExcludeList, DataListOptionSearch::setDataListKeyExcludeList),
            Pair.of(DataListOptionSearch::getOptionLikeList, DataListOptionSearch::setOptionLikeList),
            Pair.of(DataListOptionSearch::getOptionNotLikeList, DataListOptionSearch::setOptionNotLikeList),
            Pair.of(DataListOptionSearch::getOptionI18nLikeList, DataListOptionSearch::setOptionI18nLikeList),
            Pair.of(DataListOptionSearch::getOptionI18nNotLikeList, DataListOptionSearch::setOptionI18nNotLikeList),
            Pair.of(DataListOptionSearch::getExternalIdList, DataListOptionSearch::setExternalIdList),
            Pair.of(DataListOptionSearch::getExternalIdExcludeList, DataListOptionSearch::setExternalIdExcludeList),
            Pair.of(DataListOptionSearch::getBusinessAccountIdList, DataListOptionSearch::setBusinessAccountIdList),
            Pair.of(DataListOptionSearch::getBusinessAccountIdExcludeList, DataListOptionSearch::setBusinessAccountIdExcludeList),
            Pair.of(DataListOptionSearch::getDataListSubsetIdList, DataListOptionSearch::setDataListSubsetIdList),
            Pair.of(DataListOptionSearch::getDataListSubsetIdExcludeList, DataListOptionSearch::setDataListSubsetIdExcludeList),
            Pair.of(DataListOptionSearch::getDataListSubsetKeyList, DataListOptionSearch::setDataListSubsetKeyList),
            Pair.of(DataListOptionSearch::getDataListSubsetKeyExcludeList, DataListOptionSearch::setDataListSubsetKeyExcludeList),
            Pair.of(DataListOptionSearch::getStatusIdList, DataListOptionSearch::setStatusIdList),
            Pair.of(DataListOptionSearch::getStatusIdExcludeList, DataListOptionSearch::setStatusIdExcludeList),
            Pair.of(DataListOptionSearch::getValidForTwinClassFieldIdList, DataListOptionSearch::setValidForTwinClassFieldIdList)
    );

    public static final ImmutableList<Pair<Function<DataListOptionSearch, Ternary>, BiConsumer<DataListOptionSearch, Ternary>>> TERNARY_FIELD = ImmutableList.of(
            Pair.of(DataListOptionSearch::getCustom, DataListOptionSearch::setCustom)
    );
}
