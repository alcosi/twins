package org.twins.core.dto.rest.permission;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.ResponseRelatedObjectsDTOv1;
import org.twins.core.dto.rest.related.RelatedObject;
import org.twins.core.dto.rest.space.SpaceRoleUserDTOv1;
import org.twins.core.dto.rest.space.SpaceRoleUserGroupDTOv1;
import org.twins.core.dto.rest.twin.TwinDTOv2;
import org.twins.core.dto.rest.twinclass.TwinClassDTOv1;
import org.twins.core.dto.rest.twinstatus.TwinStatusDTOv1;
import org.twins.core.dto.rest.usergroup.UserGroupDTOv1;
import org.twins.core.enums.twin.TwinRole;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@Schema(name =  "PermissionCheckOverviewRsV1")
public class PermissionCheckOverviewRsDTOv1 extends ResponseRelatedObjectsDTOv1 {

    @Schema(description = "permissionId")
    @RelatedObject(type = PermissionDTOv1.class, name = "permission")
    public UUID permissionId;

    @Schema(description = "permissionGroupId")
    @RelatedObject(type = PermissionGroupDTOv1.class, name = "permissionGroup")
    public UUID permissionGroupId;

    @Schema(description = "permissionSchemaId")
    @RelatedObject(type = PermissionSchemaDTOv1.class, name = "permissionSchema")
    public UUID permissionSchemaId;

    @Schema(description = "grantedByUser")
    public boolean grantedByUser;

    @Schema(description = "grantedByUserGroupIds")
    @RelatedObject(type = UserGroupDTOv1.class, name = "grantedByUserGroupList")
    public Set<UUID> grantedByUserGroupIds;

    @Schema(description = "grantedByTwinRoles")
    public Set<TwinRole> grantedByTwinRoles;

    @Schema(description = "propagatedByTwinClass")
    public List<TwinClassDTOv1> propagatedByTwinClasses;

    @Schema(description = "propagatedByTwinStatus")
    public List<TwinStatusDTOv1> propagatedByTwinStatuses;

    @Schema(description = "grantedBySpaceRoleUserIds")
    @RelatedObject(type = TwinDTOv2.class, name = "grantedBySpaceRoleUserList")
    public Set<UUID> grantedBySpaceRoleUserIds;

    @Schema(description = "grantedBySpaceRoleUsers")
    public List<SpaceRoleUserDTOv1> grantedBySpaceRoleUsers;

    @Schema(description = "grantedBySpaceRoleUserGroupIds")
    @RelatedObject(type = TwinDTOv2.class, name = "grantedBySpaceRoleUserGroupList")
    public Set<UUID> grantedBySpaceRoleUserGroupIds;

    @Schema(description = "grantedBySpaceRoleUserGroups")
    public List<SpaceRoleUserGroupDTOv1> grantedBySpaceRoleUserGroups;
}


