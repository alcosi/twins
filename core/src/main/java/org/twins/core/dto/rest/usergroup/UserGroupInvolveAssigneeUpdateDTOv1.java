package org.twins.core.dto.rest.usergroup;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.DTOExamples;

import java.util.UUID;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Schema(name = "UserGroupInvolveAssigneeUpdateV1")
public class UserGroupInvolveAssigneeUpdateDTOv1 extends UserGroupInvolveAssigneeSaveDTOv1 {
    @Schema(description = "id", example = DTOExamples.USER_GROUP_INVOLVE_ASSIGNEE_ID)
    public UUID id;
}
