package org.twins.core.dto.rest.permission;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.Response;
import org.twins.core.dto.rest.space.SpaceRoleUserDTOv1;
import org.twins.core.dto.rest.space.SpaceRoleUserGroupDTOv1;
import org.twins.core.dto.rest.twinclass.TwinClassDTOv1;
import org.twins.core.dto.rest.twinstatus.TwinStatusDTOv1;
import org.twins.core.dto.rest.usergroup.UserGroupDTOv1;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@Schema(name =  "PermissionCheckOverviewRsV1")
public class PermissionCheckOverviewRsDTOv1 extends Response {

    @Schema(description = "permissionId")
    public UUID permissionId;

    @Schema(description = "permission")
    public PermissionDTOv1 permission;

    @Schema(description = "permissionGroupId")
    public UUID permissionGroupId;

    @Schema(description = "permissionGroup")
    public PermissionGroupDTOv1 permissionGroup;

    @Schema(description = "permissionSchemaId")
    public UUID permissionSchemaId;

    @Schema(description = "permissionSchema")
    public PermissionSchemaDTOv1 permissionSchema;

    @Schema(description = "grantedByUser")
    public boolean grantedByUser;

    @Schema(description = "grantedByUserGroupIds")
    public Set<UUID> grantedByUserGroupIds;

    @Schema(description = "grantedByUserGroups")
    public List<UserGroupDTOv1> grantedByUserGroups;

    @Schema(description = "grantedByTwinRoles")
    public Set<TwinRole> grantedByTwinRoles;

    @Schema(description = "propagatedByTwinClass")
    public List<TwinClassDTOv1> propagatedByTwinClasses;

    @Schema(description = "propagatedByTwinStatus")
    public List<TwinStatusDTOv1> propagatedByTwinStatuses;

    @Schema(description = "grantedBySpaceRoleUserIds")
    public Set<UUID> grantedBySpaceRoleUserIds;

    @Schema(description = "grantedBySpaceRoleUsers")
    public List<SpaceRoleUserDTOv1> grantedBySpaceRoleUsers;

    @Schema(description = "grantedBySpaceRoleUserGroupIds")
    public Set<UUID> grantedBySpaceRoleUserGroupIds;

    @Schema(description = "grantedBySpaceRoleUserGroups")
    public List<SpaceRoleUserGroupDTOv1> grantedBySpaceRoleUserGroups;
}
