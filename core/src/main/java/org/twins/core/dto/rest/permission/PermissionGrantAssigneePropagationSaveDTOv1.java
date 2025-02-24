package org.twins.core.dto.rest.permission;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.DTOExamples;

import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name = "PermissionGrantAssigneePropagationSaveV1")
public class PermissionGrantAssigneePropagationSaveDTOv1 {
    @Schema(description = "permission schema id", example = DTOExamples.PERMISSION_SCHEMA_ID)
    public UUID permissionSchemaId;

    @Schema(description = "permission id", example = DTOExamples.PERMISSION_ID)
    public UUID permissionId;

    @Schema(description = "propagation by twin class id", example = DTOExamples.TWIN_CLASS_ID)
    public  UUID propagationByTwinClassId;

    @Schema(description = "propagation by twin status id", example = DTOExamples.TWIN_STATUS_ID)
    public UUID propagationByTwinStatusId;

    @Schema(description = "is space only", example = DTOExamples.BOOLEAN_TRUE)
    public  Boolean inSpaceOnly;
}
