package org.twins.core.dto.rest.permission;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(fluent = true)
@Schema(name =  "PermissionGroupWithPermissionsV1")
public class PermissionGroupWithPermissionsDTOv1 extends PermissionGroupDTOv1 {
    @Schema(description = "permissions")
    public List<PermissionDTOv1> permissions;

}
