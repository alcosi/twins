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
@Schema(name = "PermissionGrantUserGroupUpdateRqV1")
public class PermissionGrantUserGroupUpdateRqDTOv1 extends Request {
    @Valid
    @Schema(description = "permission grant user group")
    public PermissionGrantUserGroupUpdateDTOv1 permissionGrantUserGroup;
}
