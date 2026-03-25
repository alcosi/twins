package org.twins.core.dto.rest.usergroup;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.ResponseRelatedObjectsDTOv1;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(name = "UserGroupInvolveAssigneeViewRsV1")
public class UserGroupInvolveAssigneeViewRsDTOv1 extends ResponseRelatedObjectsDTOv1 {
    @Schema(description = "result - user group by assignee propagation")
    public UserGroupInvolveAssigneeDTOv1 userGroupInvolveAssignee;
}
