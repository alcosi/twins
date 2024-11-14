package org.twins.core.dto.rest.permission;

import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;

import java.util.Set;
import java.util.UUID;

@Data
@Accessors(chain = true)
@FieldNameConstants
public class PermissionSchemaSearch {
    Set<UUID> idList;
    Set<UUID> idExcludeList;
    Set<String> nameLikeList;
    Set<String> nameNotLikeList;
    Set<String> descriptionLikeList;
    Set<String> descriptionNotLikeList;
    Set<UUID> businessAccountIdList;
    Set<UUID> businessAccountIdExcludeList;
    Set<UUID> createdByUserIdList;
    Set<UUID> createdByUserIdExcludeList;
}
