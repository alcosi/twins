package org.twins.core.dto.rest.permission;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(fluent = true)
@Schema(name =  "PermissionWithGroupV1")
public class PermissionWithGroupDTOv1 extends PermissionDTOv1 {
    @Schema(description = "group")
    public PermissionGroupDTOv1 group;
}
