package org.twins.core.domain.permission;

import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;

import java.util.Set;
import java.util.UUID;

@Data
@Accessors(chain = true)
@FieldNameConstants
public class PermissionSchemaUserGroupSearch {
    Set<UUID> idList;
    Set<UUID> idExcludeList;
    Set<UUID> permissionSchemaIdList;
    Set<UUID> permissionSchemaIdExcludeList;
    Set<UUID> permissionIdList;
    Set<UUID> permissionIdExcludeList;
    Set<UUID> userGroupIdList;
    Set<UUID> userGroupIdExcludeList;
    Set<UUID> grantedByUserIdList;
    Set<UUID> grantedByUserIdExcludeList;
}
