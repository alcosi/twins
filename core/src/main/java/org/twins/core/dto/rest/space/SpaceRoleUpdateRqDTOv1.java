package org.twins.core.dto.rest.space;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.Request;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Schema(name = "SpaceRoleUpdateRqV1")
public class SpaceRoleUpdateRqDTOv1 extends Request {
    @Schema(description = "space roles")
    public List<SpaceRoleUpdateDTOv1> spaceRoles;
}
