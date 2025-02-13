package org.twins.core.dto.rest.permission;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.ResponseRelatedObjectsDTOv1;

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@Schema(name =  "PermissionGroupViewRsV1")
public class PermissionGroupViewRsDTOv1 extends ResponseRelatedObjectsDTOv1 {
    @Schema(description = "permission group")
    public PermissionGroupDTOv1 permissionGroup;
}
