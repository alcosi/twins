package org.twins.core.dto.rest.user;

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
@Schema(name =  "UserV1")
public class UserDTOv1 {
    @Schema(description = "id", example = DTOExamples.USER_ID)
    public UUID id;

    @Schema(description = "fullName", example = "John Doe")
    public String fullName;

    @Schema(description = "email", example = "some@email.com")
    public String email;

    @Schema(description = "avatar url", example = "http://twins.org/a/avatar/carkikrefmkawfwfwg.png")
    public String avatar;

    @Schema(description = "an ids of user groups")
    @RelatedObject(type = UserGroupDTOv1.class, name = "userGroupList")
    public Set<UUID> userGroupIds;
}


