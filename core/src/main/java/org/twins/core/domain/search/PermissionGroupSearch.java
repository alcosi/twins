package org.twins.core.domain.search;

import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;

import java.util.Set;
import java.util.UUID;

@Data
@Accessors(chain = true)
@FieldNameConstants
public class PermissionGroupSearch {
    Set<UUID> idList;
    Set<UUID> idExcludeList;
    Set<UUID> twinClassIdList;
    Set<UUID> twinClassIdExcludeList;
    Set<String> keyLikeList;
    Set<String> keyNotLikeList;
    Set<String> nameLikeList;
    Set<String> nameNotLikeList;
    Set<String> descriptionLikeList;
    Set<String> descriptionNotLikeList;
    boolean showSystemGroups;
}
