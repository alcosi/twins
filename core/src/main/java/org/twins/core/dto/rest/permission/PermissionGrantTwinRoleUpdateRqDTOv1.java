package org.twins.core.dto.rest.permission;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.Request;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(name = "PermissionGrantTwinRoleUpdateRqV1")
public class PermissionGrantTwinRoleUpdateRqDTOv1 extends Request {
    @Schema(description = "permission grant twin role create")
    public PermissionGrantTwinRoleUpdateDTOv1 permissionGrantTwinRole;
}