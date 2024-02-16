package org.twins.core.domain.system;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.Response;
import org.twins.core.dto.rest.space.SpaceRoleUserDTOv1;
import org.twins.core.dto.rest.user.UserDTOv1;

import java.util.List;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Schema(name = "UserWithinSpaceRolesListRsV1")
public class UserWithinSpaceRolesListRsDTOv1 extends Response {

    @Schema(description = "user")
    public UserDTOv1 user;

    @Schema(description = "space role list")
    public List<SpaceRoleUserDTOv1> spaceRoleUserList;

}
