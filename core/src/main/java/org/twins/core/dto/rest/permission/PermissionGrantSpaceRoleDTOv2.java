package org.twins.core.dto.rest.permission;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.space.SpaceRoleDTOv2;
import org.twins.core.dto.rest.user.UserDTOv1;

import java.util.UUID;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Schema(name = "PermissionGrantSpaceRoleV2")
public class PermissionGrantSpaceRoleDTOv2 extends PermissionGrantSpaceRoleDTOv1 {
    @Schema(description = "permission schema")
    public PermissionSchemaDTOv1 permissionSchema;

    @Schema(description = "permission")
    public PermissionDTOv1 permission;

    @Schema(description = "space role")
    public SpaceRoleDTOv2 spaceRole;

    @Schema(description = "granted by user")
    public UserDTOv1 grantedByUser;
}
