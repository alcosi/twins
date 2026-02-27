package org.twins.core.dto.rest.permission;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.DTOConfig;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.related.RelatedObject;
import org.twins.core.dto.rest.twinclass.TwinClassDTOv1;
import org.twins.core.dto.rest.user.UserDTOv1;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name = "PermissionGrantTwinRoleV1")
public class PermissionGrantTwinRoleDTOv1 {
    @Schema(description = "id", example = DTOExamples.PERMISSION_GRANT_TWIN_ROLE_ID)
    public UUID id;

    @Schema(description = "permission schema id", example = DTOExamples.PERMISSION_SCHEMA_ID)
    @RelatedObject(type = PermissionSchemaDTOv1.class, name = "permissionSchema")
    public UUID permissionSchemaId;

    @Schema(description = "permission id", example = DTOExamples.PERMISSION_ID)
    @RelatedObject(type = PermissionDTOv1.class, name = "permission")
    public UUID permissionId;

    @Schema(description = "twin class id", example = DTOExamples.TWIN_CLASS_ID)
    @RelatedObject(type = TwinClassDTOv1.class, name = "twinClass")
    public UUID twinClassId;

    @Schema(description = "granted by user id", example = DTOExamples.USER_ID)
    @RelatedObject(type = UserDTOv1.class, name = "grantedByUser")
    public UUID grantedByUserId;

    @Schema(description = "is granted to assignee")
    private Boolean grantedToAssignee;

    @Schema(description = "is granted to space assignee")
    private Boolean grantedToSpaceAssignee;

    @Schema(description = "is granted to creator")
    private Boolean grantedToCreator;

    @Schema(description = "is granted to space creator")
    private Boolean grantedToSpaceCreator;

    @JsonFormat(pattern = DTOConfig.DATE_FORMAT)
    @Schema(description = "granted at", example = DTOExamples.INSTANT)
    public LocalDateTime grantedAt;
}
