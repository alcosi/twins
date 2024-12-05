package org.twins.core.dto.rest.permission;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.domain.TwinRole;
import org.twins.core.dto.rest.DTOExamples;

import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name = "PermissionGrantTwinRoleV1")
public class PermissionGrantTwinRoleDTOv1 {
    @Schema(description = "id", example = DTOExamples.PERMISSION_GRANT_TWIN_ROLE_ID)
    public UUID id;

    @Schema(description = "permission schema id", example = DTOExamples.PERMISSION_SCHEMA_ID)
    public UUID permissionSchemaId;

    @Schema(description = "permission id", example = DTOExamples.PERMISSION_ID)
    public UUID permissionId;

    @Schema(description = "twin class id", example = DTOExamples.TWIN_CLASS_ID)
    public UUID twinClassId;

    @Schema(description = "granted by user id", example = DTOExamples.USER_ID)
    public UUID grantedByUserId;

    @Schema(description = "twin role")
    public TwinRole twinRole;
}
