package org.twins.core.dto.rest.permission;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.ResponseRelatedObjectsDTOv1;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(name = "PermissionGrantUserGroupViewRsV1")
public class PermissionGrantUserGroupViewRsDTOv1 extends ResponseRelatedObjectsDTOv1 {
    @Schema(description = "result - permission grant user-group")
    public PermissionGrantUserGroupDTOv1 permissionGrantUserGroup;
}
