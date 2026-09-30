package org.twins.core.dto.rest.permission;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.Request;

import java.util.List;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(name = "PermissionUpdateRqV1")
public class PermissionUpdateRqDTOv1 extends Request {
    @Valid
    @Schema(description = "permission update list")
    public List<PermissionUpdateDTOv1> permissions;
}
