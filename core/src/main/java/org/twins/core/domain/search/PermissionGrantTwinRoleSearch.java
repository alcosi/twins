package org.twins.core.domain.search;

import lombok.Data;
import lombok.experimental.Accessors;
import org.cambium.common.util.Ternary;
import org.twins.core.enums.twin.TwinRole;

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
    Ternary isAssignee;
    ...
    Set<TwinRole> twinRoleList;
    Set<TwinRole> twinRoleExcludeList;
    Set<UUID> grantedByUserIdList;
    Set<UUID> grantedByUserIdExcludeList;
}
