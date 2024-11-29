package org.twins.core.dto.rest.permission;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.DTOExamples;

import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name = "PermissionGrantAssigneePropagationV1")
public class PermissionGrantAssigneePropagationDTOv1 {
    @Schema(description = "id", example = DTOExamples.PERMISSION_GRANT_USER_ID)
    public UUID id;

    @Schema(description = "permission schema id", example = DTOExamples.PERMISSION_SCHEMA_ID)
    public UUID permissionSchemaId;

    @Schema(description = "permission id", example = DTOExamples.PERMISSION_ID)
    public UUID permissionId;

    @Schema(description = "propagation twin class id", example = DTOExamples.TWIN_CLASS_ID)
    public UUID propagationTwinClassId;

    @Schema(description = "twin status id", example = DTOExamples.TWIN_STATUS_ID)
    public UUID propagationTwinStatusId;

    @Schema(description = "granted by user id", example = DTOExamples.USER_ID)
    public UUID grantedByUserId;
}
