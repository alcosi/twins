package org.twins.core.dto.rest.usergroup;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.ResponseRelatedObjectsDTOv1;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(name = "UserGroupByAssigneePropagationRsV1")
public class UserGroupByAssigneePropagationRsDTOv1 extends ResponseRelatedObjectsDTOv1 {
    @Schema(description = "results -  user group by assignee propagation")
    public UserGroupByAssigneePropagationDTOv1 userGroupByAssigneePropagation;
}
