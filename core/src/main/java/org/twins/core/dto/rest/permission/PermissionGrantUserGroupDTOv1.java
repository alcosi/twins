package org.twins.core.dto.rest.permission;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.DTOConfig;
import org.twins.core.dto.rest.DTOExamples;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name =  "PermissionGrantUserGroupV1")
public class PermissionGrantUserGroupDTOv1 {
    @Schema(description = "id", example = DTOExamples.PERMISSION_GRANT_USER_GROUP_ID)
    public UUID id;

    @Schema(description = "permission schema id", example = DTOExamples.PERMISSION_SCHEMA_ID)
    public UUID permissionSchemaId;

    @Schema(description = "permission id", example = DTOExamples.PERMISSION_ID)
    public UUID permissionId;

    @Schema(description = "user group id", example = DTOExamples.USER_GROUP_ID)
    public UUID userGroupId;

    @Schema(description = "granted by user id", example = DTOExamples.USER_ID)
    public UUID grantedByUserId;

    @JsonFormat(pattern = DTOConfig.DATE_FORMAT)
    @Schema(description = "granted at", example = DTOExamples.INSTANT)
    public LocalDateTime grantedAt;
}
