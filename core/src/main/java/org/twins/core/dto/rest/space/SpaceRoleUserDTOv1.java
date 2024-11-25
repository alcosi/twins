package org.twins.core.dto.rest.space;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.DTOExamples;

import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name = "SpaceRoleUserV1")
public class SpaceRoleUserDTOv1 {
    @Schema(description = "id", example = DTOExamples.SPACE_ROLE_USER_ID)
    public UUID id;

    @Schema(description = "twin id", example = DTOExamples.TWIN_ID)
    public UUID twinId;

    @Schema(description = "space role id", example = DTOExamples.SPACE_ROLE)
    public UUID spaceRoleId;

    @Schema(description = "user id", example = DTOExamples.USER_ID)
    public UUID userId;

    @Schema(description = "created by user id", example = DTOExamples.USER_ID)
    public UUID createdByUserId;

    @Schema(description = "space role")
    public SpaceRoleDTOv2 spaceRole;

}
