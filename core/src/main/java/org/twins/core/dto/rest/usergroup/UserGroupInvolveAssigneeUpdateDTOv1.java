package org.twins.core.dto.rest.usergroup;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.DTOExamples;

import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name = "UserGroupInvolveAssigneeUpdateV1")
public class UserGroupInvolveAssigneeUpdateDTOv1 {
    @NotNull
    @Schema(description = "id", example = DTOExamples.USER_GROUP_INVOLVE_ASSIGNEE_ID)
    public UUID id;

    @Schema(description = "user group id", example = DTOExamples.USER_GROUP_ID)
    public UUID userGroupId;

    @Schema(description = "propagation by twin class id", example = DTOExamples.TWIN_CLASS_ID)
    public UUID propagationByTwinClassId;

    @Schema(description = "propagation by twin status id", example = DTOExamples.TWIN_STATUS_ID)
    public UUID propagationByTwinStatusId;

    @Schema(description = "is space only", example = DTOExamples.BOOLEAN_TRUE)
    public Boolean inSpaceOnly;
}
