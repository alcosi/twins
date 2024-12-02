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
    Set<UUID> idList;
    Set<UUID> idExcludeList;
    Set<UUID> dataListIdList;
    Set<UUID> dataListIdExcludeList;
    Set<String> optionLikeList;
    Set<String> optionNotLikeList;
    Set<String> optionI18nLikeList;
    Set<String> optionI18nNotLikeList;
    Set<UUID> businessAccountIdList;
    Set<UUID> businessAccountIdExcludeList;
}
