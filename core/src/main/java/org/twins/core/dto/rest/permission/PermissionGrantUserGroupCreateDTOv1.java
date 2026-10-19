package org.twins.core.dto.rest.permission;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.DTOExamples;

import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name = "PermissionGrantUserGroupCreateV1")
public class PermissionGrantUserGroupCreateDTOv1 {
    @NotNull
    @Schema(description = "permission schema id", example = DTOExamples.PERMISSION_SCHEMA_ID)
    public UUID permissionSchemaId;

    @NotNull
    @Schema(description = "permission id", example = DTOExamples.PERMISSION_ID)
    public UUID permissionId;

    @NotNull
    @Schema(description = "user group id", example = DTOExamples.USER_GROUP_ID)
    public UUID userGroupId;
}
