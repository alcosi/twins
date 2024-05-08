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
@Schema(name = "SpaceRoleUserRqV1")
public class SpaceRoleUserGroupDTOv1 extends Request {

    @Schema(description = "")
    private UUID id;

    @Schema(description = "")
    private UUID twinId;

    @Schema(description = "")
    private UUID spaceRoleId;

    @Schema(description = "")
    private UUID userGroupId;

    @Schema(description = "")
    private UUID createdByUserId;

    @Schema(description = "")
    private SpaceRoleDTOv1 spaceRole;
}
