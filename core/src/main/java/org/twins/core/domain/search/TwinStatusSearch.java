package org.twins.core.domain.search;

import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;

import java.util.Set;
import java.util.UUID;

@Data
@Accessors(chain = true)
@FieldNameConstants
public class TwinStatusSearch {
    Set<UUID> idList;
    Set<UUID> idExcludeList;
    Set<UUID> twinClassIdList;
    Set<UUID> twinClassIdExcludeList;
    Set<String> keyLikeList;
    Set<String> keyNotLikeList;
    Set<String> nameI18nLikeList;
    Set<String> nameI18nNotLikeList;
    Set<String> descriptionI18nLikeList;
    Set<String> descriptionI18nNotLikeList;
}
