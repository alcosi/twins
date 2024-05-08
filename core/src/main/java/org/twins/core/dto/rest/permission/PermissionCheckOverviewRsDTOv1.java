package org.twins.core.dto.rest.permission;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.Response;
import org.twins.core.dto.rest.space.SpaceRoleDTOv1;
import org.twins.core.dto.rest.space.SpaceRoleUserDTOv1;
import org.twins.core.dto.rest.space.SpaceRoleUserGroupDTOv1;
import org.twins.core.dto.rest.usergroup.UserGroupDTOv1;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@Schema(name =  "PermissionCheckOverviewV1")
public class PermissionCheckOverviewRsDTOv1 extends Response {

    @Schema(description = "permissionId")
    public UUID permissionId;

    @Schema(description = "permissionId")
    public Set<UUID> permissionSchemaIds;

    @Schema(description = "permissionGroupId")
    public UUID permissionGroupId;

    @Schema(description = "permission")
    public PermissionDTOv1 permission;

    @Schema(description = "permissionSchema")
    public List<PermissionSchemaDTOv1> permissionSchemas;

    @Schema(description = "permissionGroup")
    public PermissionGroupDTOv1 permissionGroup;

    @Schema(description = "grantedByUser")
    public boolean grantedByUser;

    @Schema(description = "grantedByUserGroupIds")
    public Set<UUID> grantedByUserGroupIds;

    @Schema(description = "grantedByUserGroups")
    public List<UserGroupDTOv1> grantedByUserGroups;

    @Schema(description = "grantedByTwinRoles")
    public Set<TwinRole> grantedByTwinRoles;

    @Schema(description = "spaceRoles")
    public List<SpaceRoleDTOv1> spaceRoles;

    @Schema(description = "grantedBySpaceRoleUserIds")
    public Set<UUID> grantedBySpaceRoleUserIds;

    @Schema(description = "grantedBySpaceRoleUsers")
    public List<SpaceRoleUserDTOv1> grantedBySpaceRoleUsers;

    @Schema(description = "grantedBySpaceRoleUserGroupIds")
    public Set<UUID> grantedBySpaceRoleUserGroupIds;

    @Schema(description = "grantedBySpaceRoleUserGroups")
    public List<SpaceRoleUserGroupDTOv1> grantedBySpaceRoleUserGroups;
}
