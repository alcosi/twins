package org.twins.core.dto.rest.usergroup;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.DTOExamples;

import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name = "UserGroupByAssigneePropagationSaveV1")
public class UserGroupByAssigneePropagationSaveDTOv1 {
    @Schema(description = "permission schema id", example = DTOExamples.PERMISSION_SCHEMA_ID)
    public UUID permissionSchemaId;

    @Schema(description = "user group id", example = DTOExamples.USER_GROUP_ID)
    public UUID userGroupId;

    @Schema(description = "propagation by twin class id", example = DTOExamples.TWIN_CLASS_ID)
    public  UUID propagationByTwinClassId;

    @Schema(description = "propagation by twin status id", example = DTOExamples.TWIN_STATUS_ID)
    public UUID propagationByTwinStatusId;

    @Schema(description = "is space only", example = DTOExamples.BOOLEAN_TRUE)
    public  Boolean inSpaceOnly;
}
