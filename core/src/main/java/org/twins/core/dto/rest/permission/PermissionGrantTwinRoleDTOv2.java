package org.twins.core.dto.rest.permission;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.twinclass.TwinClassDTOv1;
import org.twins.core.dto.rest.user.UserDTOv1;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Schema(name = "PermissionGrantTwinRoleV1")
public class PermissionGrantTwinRoleDTOv2 extends PermissionGrantTwinRoleDTOv1 {
    @Schema(description = "permission")
    public PermissionDTOv1 permission;

    @Schema(description = "permission schema")
    public PermissionSchemaDTOv1 permissionSchema;

    @Schema(description = "twin class")
    public TwinClassDTOv1 twinClass;

    @Schema(description = "granted by user")
    public UserDTOv1 grantedByUser;
}
