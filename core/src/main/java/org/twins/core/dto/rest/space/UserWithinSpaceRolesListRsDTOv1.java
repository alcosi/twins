package org.twins.core.dto.rest.space;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.Response;

import java.util.List;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Schema(name = "UserWithinSpaceRolesListRsV1")
public class UserWithinSpaceRolesListRsDTOv1 extends Response {

    @Schema(description = "space role list")
    public List<UserWithinSpaceRolesRsDTOv1> spaceRoleUserList;

}
