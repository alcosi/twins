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
    private Set<UUID> idList;
    private Set<UUID> idExcludeList;
    private Set<String> nameLikeList;
    private Set<String> nameNotLikeList;
    private Set<String> descriptionLikeList;
    private Set<String> descriptionNotLikeList;
    private Set<String> keyLikeList;
    private Set<String> keyNotLikeList;
    private DataListOptionSearch optionSearch;
    private Set<String> externalIdLikeList;
    private Set<String> externalIdNotLikeList;
    private Set<UUID> defaultOptionIdList;
    private Set<UUID> defaultOptionIdExcludeList;
}
