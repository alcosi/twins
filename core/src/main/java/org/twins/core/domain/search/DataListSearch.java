package org.twins.core.domain.search;

import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;

import java.util.Set;
import java.util.UUID;

@Data
@Accessors(chain = true)
@FieldNameConstants
public class DataListSearch {
    Set<UUID> idList;
    Set<UUID> idExcludeList;
    Set<String> nameLikeList;
    Set<String> nameNotLikeList;
    Set<String> descriptionLikeList;
    Set<String> descriptionNotLikeList;
    Set<String> keyLikeList;
    Set<String> keyNotLikeList;
    Set<String> optionLikeList;
    Set<String> optionNotLikeList;
    Set<String> optionI18nLikeList;
    Set<String> optionI18nNotLikeList;
}
