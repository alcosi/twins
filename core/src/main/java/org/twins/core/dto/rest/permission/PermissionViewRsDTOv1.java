package org.twins.core.dto.rest.permission;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.ResponseRelatedObjectsDTOv1;

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@Schema(name =  "PermissionViewRsV1")
public class PermissionViewRsDTOv1 extends ResponseRelatedObjectsDTOv1 {
    @Schema(description = "permission ")
    public PermissionDTOv1 permission;
}
