package org.twins.core.dto.rest.usergroup;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.ResponseRelatedObjectsDTOv1;

import java.util.List;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(name = "UserGroupInvolveAssigneeListRsV1")
public class UserGroupInvolveAssigneeListRsDTOv1 extends ResponseRelatedObjectsDTOv1 {
    @Schema(description = "user group by assignee propagation list")
    public List<UserGroupInvolveAssigneeDTOv1> userGroupInvolveAssigneeList;
}
