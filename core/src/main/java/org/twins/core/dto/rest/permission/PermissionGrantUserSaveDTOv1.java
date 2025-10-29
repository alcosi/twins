package org.twins.core.dto.rest.permission;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.DTOExamples;

import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name = "PermissionGrantUserSaveV1")
public class PermissionGrantUserSaveDTOv1 {
    @Schema(description = "permission schema id", example = DTOExamples.PERMISSION_SCHEMA_ID)
    public UUID permissionSchemaId;
    @Schema(description = "permission id", example = DTOExamples.PERMISSION_ID)
    public UUID permissionId;
    @Schema(description = "business account id", example = DTOExamples.USER_ID)
    public UUID businessAccountId;
    @Schema(description = "user id", example = DTOExamples.USER_ID)
    public UUID userId;
}
