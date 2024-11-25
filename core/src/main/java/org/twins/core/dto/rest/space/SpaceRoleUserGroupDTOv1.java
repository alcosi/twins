package org.twins.core.dto.rest.space;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.Request;

import java.util.UUID;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(name = "SpaceRoleUserGroupV1")
public class SpaceRoleUserGroupDTOv1 extends Request {

    @Schema(description = "")
    public UUID id;

    @Schema(description = "")
    public UUID twinId;

    @Schema(description = "")
    public UUID spaceRoleId;

    @Schema(description = "")
    public UUID userGroupId;

    @Schema(description = "")
    public UUID createdByUserId;

    @Schema(description = "")
    public SpaceRoleDTOv2 spaceRole;
}
