package org.twins.core.dto.rest.space;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.Request;

import java.util.List;
import java.util.UUID;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Schema(name =  "UserRefSpaceRoleSearchV1")
public class UserRefSpaceRoleSearchDTOv1 extends Request {
    @Schema(description = "Filter by user name, case ignore", example = "st Us")
    public String userNameLike;

    @Schema(description = "Filter by specific roles(uuids)", example = "[\"793e3120-e14a-4a22-ab09-060b9fedee35\"]")
    public List<UUID> spaceRolesList;

    @Schema(description = "Filter users from specific group", example = "[\"793e3120-e14a-4a22-ab09-060b9fedee35\"]")
    public List<UUID> userGroupIdList;
}
