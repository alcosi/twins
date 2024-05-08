package org.twins.core.domain.permission;

import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dao.permission.PermissionEntity;
import org.twins.core.dao.permission.PermissionSchemaEntity;
import org.twins.core.dao.space.SpaceRoleUserEntity;
import org.twins.core.dao.space.SpaceRoleUserGroupEntity;
import org.twins.core.dao.user.UserGroupEntity;
import org.twins.core.dto.rest.permission.TwinRole;

import java.util.List;
import java.util.Set;

@Data
@Accessors(chain = true)
public class PermissionCheckForTwinOverviewResult {
    private PermissionEntity permission;
    private PermissionSchemaEntity permissionSchemaEntity;

    private boolean grantedByUser;
    private List<UserGroupEntity> grantedByUserGroups;
    private Set<TwinRole> grantedByTwinRoles;
    private List<SpaceRoleUserEntity> grantedBySpaceRoleUsers;
    private List<SpaceRoleUserGroupEntity> grantedBySpaceRoleUserGroups;
}
