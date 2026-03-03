package org.twins.core.dto.rest.usergroup;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.Request;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(name = "UserGroupByAssigneePropagationUpdateRqV1")
public class UserGroupByAssigneePropagationUpdateRqDTOv1 extends Request {
    @Schema(description = "user group by assignee propagation")
    public UserGroupByAssigneePropagationUpdateDTOv1 userGroupByAssigneePropagation;
}
