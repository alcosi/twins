package org.twins.core.dto.rest.space;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.Request;

import java.util.UUID;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Schema(name = "SpaceRoleUserV1")
public class SpaceRoleUserDTOv1 extends Request {

    @Schema(description = "")
    public UUID id;

    @Schema(description = "")
    public UUID twinId;

    @Schema(description = "")
    public UUID spaceRoleId;

    @Schema(description = "")
    public UUID userId;

    @Schema(description = "")
    public UUID createdByUserId;

    @Schema(description = "")
    public SpaceRoleDTOv1 spaceRole;

}
