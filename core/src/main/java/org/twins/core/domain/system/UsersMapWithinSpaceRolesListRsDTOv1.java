package org.twins.core.domain.system;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.Response;

import java.util.Map;
import java.util.UUID;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Schema(name = "UsersMapWithinSpaceRolesListRsV1")
public class UsersMapWithinSpaceRolesListRsDTOv1 extends Response {

    @Schema(description = "space role users map { user id / user&space role object } ")
    public Map<UUID, UserWithinSpaceRolesListRsDTOv1> spaceRoleUsersMap;

}
