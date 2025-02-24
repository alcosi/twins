package org.twins.core.dto.rest.permission;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.Request;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(name = "PermissionGrantSpaceRoleUpdateRqV1")
public class PermissionGrantSpaceRoleUpdateRqDTOv1 extends Request {
    @Schema(description = "permission grant space role create")
    public PermissionGrantSpaceRoleUpdateDTOv1 permissionGrantSpaceRole;
}