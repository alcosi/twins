package org.twins.core.dto.rest.twin;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Set;
import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name = "TwinSearchBySpaceRoleUserV1")
public class TwinSearchBySpaceRoleUserDTOv1 {
    @Schema(description = "Space role id")
    public UUID spaceRoleId;

    @Schema(description = "User id list")
    public Set<UUID> userIdList;
}
