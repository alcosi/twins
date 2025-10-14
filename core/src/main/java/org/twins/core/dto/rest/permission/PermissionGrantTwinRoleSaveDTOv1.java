package org.twins.core.dto.rest.permission;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.enums.twin.TwinRole;
import org.twins.core.dto.rest.DTOExamples;

import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name = "PermissionGrantTwinRoleSaveV1")
public class PermissionGrantTwinRoleSaveDTOv1 {
    @Schema(description = "permission schema id", example = DTOExamples.PERMISSION_SCHEMA_ID)
    public UUID permissionSchemaId;

    @Schema(description = "permission id", example = DTOExamples.PERMISSION_ID)
    public UUID permissionId;

    @Schema(description = "twin class id", example = DTOExamples.TWIN_CLASS_ID)
    public UUID twinClassId;

    @Schema(description = "twin role", example = DTOExamples.TWIN_ROLE_ID)
    public TwinRole twinRole;
}
