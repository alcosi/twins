package org.twins.core.dto.rest.usergroup;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.Request;

import java.util.List;

@EqualsAndHashCode(callSuper = false)
@Data
@Accessors(chain = true)
@Schema(name = "UserGroupUpdateRqV1")
public class UserGroupUpdateRqDTOv1 extends Request {
    @Schema(description = "list of updated user group")
    List<UserGroupUpdateDTOv1> userGroups;
}
