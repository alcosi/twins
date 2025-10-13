package org.twins.core.dto.rest.permission;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.ResponseRelatedObjectsDTOv1;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(name = "PermissionGrantUserSaveRsV1")
public class PermissionGrantUserSaveRsDTOV1 extends ResponseRelatedObjectsDTOv1 {
    @Schema(description = "permission grant user")
    public PermissionGrantUserDTOv1 permissionGrantUser;
}
