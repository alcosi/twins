package org.twins.core.dto.rest.permission;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Set;
import java.util.UUID;

@Data
@Accessors(chain = true)
public class PermissionGrantTwinRoleSearch {
    Set<UUID> idList;
    Set<UUID> idExcludeList;
    Set<UUID> permissionSchemaIdList;
    Set<UUID> permissionSchemaIdExcludeList;
    Set<UUID> permissionIdList;
    Set<UUID> permissionIdExcludeList;
    Set<UUID> twinClassIdList;
    Set<UUID> twinClassIdExcludeList;
    Set<TwinRole> twinRoleList;
    Set<TwinRole> twinRoleExcludeList;
    Set<UUID> grantedByUserIdList;
    Set<UUID> grantedByUserIdExcludeList;
}