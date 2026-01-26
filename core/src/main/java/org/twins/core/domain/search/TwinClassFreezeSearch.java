package org.twins.core.domain.search;

import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;

import java.util.Set;
import java.util.UUID;

@Data
@Accessors(chain = true)
@FieldNameConstants
public class TwinClassFreezeSearch {
    private Set<UUID> idList;
    private Set<UUID> idExcludeList;
    private Set<String> keyLikeList;
    private Set<String> keyNotLikeList;
    private Set<UUID> statusIdList;
    private Set<UUID> statusIdExcludeList;
    private Set<String> nameLikeList;
    private Set<String> nameNotLikeList;
    private Set<String> descriptionLikeList;
    private Set<String> descriptionNotLikeList;
}
