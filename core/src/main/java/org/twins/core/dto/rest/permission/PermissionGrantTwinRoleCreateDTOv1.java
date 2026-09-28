package org.twins.core.dto.rest.permission;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.DTOExamples;

import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name = "PermissionGrantTwinRoleCreateV1")
public class PermissionGrantTwinRoleCreateDTOv1 {
    @NotNull
    @Schema(description = "permission schema id", example = DTOExamples.PERMISSION_SCHEMA_ID)
    public UUID permissionSchemaId;

    @NotNull
    @Schema(description = "permission id", example = DTOExamples.PERMISSION_ID)
    public UUID permissionId;

    @NotNull
    @Schema(description = "twin class id", example = DTOExamples.TWIN_CLASS_ID)
    public UUID twinClassId;

    @Schema(description = "granted to assignee", example = DTOExamples.BOOLEAN_TRUE)
    private Boolean grantedToAssignee;

    @Schema(description = "granted to space assignee", example = DTOExamples.BOOLEAN_TRUE)
    private Boolean grantedToSpaceAssignee;

    @Schema(description = "granted to creator", example = DTOExamples.BOOLEAN_TRUE)
    private Boolean grantedToCreator;

    @Schema(description = "granted to space creator", example = DTOExamples.BOOLEAN_TRUE)
    private Boolean grantedToSpaceCreator;
}
