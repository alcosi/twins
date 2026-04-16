package org.twins.core.dto.rest.usergroup;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.Request;

import java.util.List;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(name = "UserGroupInvolveAssigneeCreateRqV1")
public class UserGroupInvolveAssigneeCreateRqDTOv1 extends Request {
    @Schema(description = "list of user group by assignee propagation")
    public List<UserGroupInvolveAssigneeCreateDTOv1> userGroupInvolveAssignees;
}
