package org.twins.core.dto.rest.permission;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.Request;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(name = "PermissionGrantSpaceRoleCreateRqV1")
public class PermissionGrantSpaceRoleCreateRqDTOv1 extends Request {
    @Valid
    @Schema(description = "permission grant space role create")
    public PermissionGrantSpaceRoleCreateDTOv1 permissionGrantSpaceRole;
}
