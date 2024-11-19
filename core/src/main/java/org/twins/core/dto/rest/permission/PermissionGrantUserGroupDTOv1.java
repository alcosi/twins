package org.twins.core.dto.rest.permission;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.user.UserDTOv1;
import org.twins.core.dto.rest.usergroup.UserGroupDTOv1;

import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name =  "PermissionGrantUserGroupV1")
public class PermissionGrantUserGroupDTOv1 {
    public UUID id;

    @Schema(description = "permission schema id", example = DTOExamples.PERMISSION_SCHEMA_ID)
    public UUID permissionSchemaId;

    @Schema(description = "permission id", example = DTOExamples.PERMISSION_ID)
    public UUID permissionId;

    @Schema(description = "user group id", example = DTOExamples.USER_GROUP_ID)
    public UUID userGroupId;

    @Schema(description = "granted by user id", example = DTOExamples.USER_ID)
    public UUID grantedByUserId;
}
