package org.twins.core.domain.permission;

import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dao.permission.PermissionEntity;
import org.twins.core.dao.permission.PermissionGroupEntity;
import org.twins.core.dao.permission.PermissionSchemaEntity;
import org.twins.core.dao.space.SpaceRoleUserEntity;
import org.twins.core.dao.space.SpaceRoleUserGroupEntity;
import org.twins.core.dao.user.UserGroupEntity;
import org.twins.core.dto.rest.permission.TwinRole;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Data
@Accessors(chain = true)
public class PermissionCheckOverviewResult {
    public UUID permissionId;
    public Set<UUID> permissionSchemaIds;
    public UUID permissionGroupId;

    public PermissionEntity permission;
    public List<PermissionSchemaEntity> permissionSchemas;
    public PermissionGroupEntity permissionGroup;

    public boolean grantedByUser;
    public Set<UUID> grantedByUserGroupIds;
    public List<UserGroupEntity> grantedByUserGroups;

    public Set<TwinRole> grantedByTwinRoles;

    public Set<UUID> grantedBySpaceRoleUserIds;
    public List<SpaceRoleUserEntity> grantedBySpaceRoleUsers;
    public Set<UUID> grantedBySpaceRoleUserGroupIds;
    public List<SpaceRoleUserGroupEntity> grantedBySpaceRoleUserGroups;
}
