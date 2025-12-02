package org.twins.core.dto.rest.permission;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.ResponseRelatedObjectsDTOv1;

import java.util.List;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Schema(name = "PermissionListRsV1")
public class PermissionListRsDTOv1 extends ResponseRelatedObjectsDTOv1 {
    @Schema(description = "permission list")
    public List<PermissionDTOv1> permissions;
}
