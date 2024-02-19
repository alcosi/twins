package org.twins.core.dto.rest.space;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.Response;
import org.twins.core.dto.rest.user.UserDTOv1;

import java.util.List;
import java.util.UUID;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Schema(name = "UserWithinSpaceRolesRsV1")
public class UserWithinSpaceRolesRsDTOv1 extends Response {

    @Schema(description = "user id")
    public UUID id;

    @Schema(description = "user")
    public UserDTOv1 user;

    @Schema(description = "space role list")
    public List<SpaceRoleUserDTOv1> spaceRoleUserList;

}
