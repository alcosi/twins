package org.twins.core.dto.rest.space;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.ResponseRelatedObjectsDTOv1;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Schema(name = "UserWithinSpaceRolesViewRsDTOv1")
public class UserWithinSpaceRolesViewRsDTOv1 extends ResponseRelatedObjectsDTOv1 {
    @Schema(description = "space roles")
    public UserWithinSpaceRolesRsDTOv1 userRefSpaceRoles;
}
