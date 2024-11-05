package org.twins.core.dto.rest.permission;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.Response;
import org.twins.core.dto.rest.twinstatus.TwinStatusDTOv1;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Schema(name = "PermissionSaveRsV1")
public class PermissionSaveRsDTOv1 extends Response {
    @Schema(description = "permission")
    public PermissionDTOv1 permission;
}
