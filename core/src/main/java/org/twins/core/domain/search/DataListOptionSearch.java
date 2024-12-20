package org.twins.core.domain.search;

import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;

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
    private Set<UUID> businessAccountIdList;
    private Set<UUID> businessAccountIdExcludeList;
    private Set<UUID> dataListSubsetIdList;
    private Set<UUID> dataListSubsetIdExcludeList;
    private Set<String> dataListSubsetOptionList;
    private Set<String> dataListSubsetOptionExcludeList;
}
