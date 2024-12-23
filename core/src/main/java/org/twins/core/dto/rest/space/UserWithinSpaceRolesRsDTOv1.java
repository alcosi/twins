package org.twins.core.dto.rest.space;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.user.UserDTOv1;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name = "UserWithinSpaceRolesRsV1")
public class UserWithinSpaceRolesRsDTOv1 {

    @Schema(description = "user id")
    public UUID userId;

    @Schema(description = "user")
    public UserDTOv1 user;

    @Schema(description = "space role list")
    public List<SpaceRoleDTOv2> spaceRoleList;

    @Schema(description = "spaceRoleIds list." + DTOExamples.LAZY_RELATION_MODE_OFF)
    public Set<UUID> spaceRoleIdsList;

}
