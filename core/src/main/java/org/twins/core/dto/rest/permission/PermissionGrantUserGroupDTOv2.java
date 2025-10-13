package org.twins.core.dto.rest.permission;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.user.UserDTOv1;
import org.twins.core.dto.rest.usergroup.UserGroupDTOv1;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Schema(name = "PermissionGrantUserGroupV2")
public class PermissionGrantUserGroupDTOv2 extends PermissionGrantUserGroupDTOv1 {
    @Schema(description = "permission schema")
    public PermissionSchemaDTOv1 permissionSchema;

    @Schema(description = "permission")
    public PermissionDTOv1 permission;

    @Schema(description = "user group")
    public UserGroupDTOv1 userGroup;

    @Schema(description = "granted by user")
    public UserDTOv1 grantedByUser;
}
