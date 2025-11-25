package org.twins.core.dto.rest.space;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.related.RelatedObject;
import org.twins.core.dto.rest.usergroup.UserGroupDTOv1;

import java.util.Set;
import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name = "UserWithinSpaceRolesRsV1")
public class UserWithinSpaceRolesRsDTOv1 {

    @Schema(description = "user id")
    @RelatedObject(type = UserGroupDTOv1.class, name = "user")
    public UUID userId;

    @Schema(description = "spaceRoleIds list." + DTOExamples.LAZY_RELATION_MODE_OFF)
    @RelatedObject(type = SpaceRoleDTOv1.class, name = "spaceRoleList")
    public Set<UUID> spaceRoleIdsList;

}


