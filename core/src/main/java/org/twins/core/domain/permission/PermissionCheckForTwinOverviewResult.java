package org.twins.core.domain.permission;

import lombok.Data;
import lombok.experimental.Accessors;
import org.cambium.common.kit.Kit;
import org.twins.core.dao.permission.PermissionEntity;
import org.twins.core.dao.permission.PermissionSchemaEntity;
import org.twins.core.dao.space.SpaceRoleUserEntity;
import org.twins.core.dao.space.SpaceRoleUserGroupEntity;
import org.twins.core.dao.twin.TwinStatusEntity;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.dao.user.UserGroupEntity;
import org.twins.core.enums.twin.TwinRole;

import java.util.Set;
import java.util.UUID;

@Data
@Accessors(chain = true)
public class PermissionCheckForTwinOverviewResult {
    private PermissionEntity permission;
    private PermissionSchemaEntity permissionSchema;
    private boolean grantedByUser;
    private Kit<UserGroupEntity, UUID> grantedByUserGroups;
    private Set<TwinRole> grantedByTwinRoles;
    private Kit<TwinClassEntity, UUID> propagatedByTwinClasses;
    private Kit<TwinStatusEntity, UUID> propagatedByTwinStatuses;
    private Kit<SpaceRoleUserEntity, UUID> grantedBySpaceRoleUsers;
    private Kit<SpaceRoleUserGroupEntity, UUID> grantedBySpaceRoleUserGroups;
}
