package org.twins.core.dto.rest.permission;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.DTOExamples;

import java.util.UUID;

@Data
@Accessors(fluent = true)
@Schema(name = "TwinBaseV1")
public class PermissionCheckOverviewRqDTOv1 {

    @Schema(description = "userId for whom we want to check permission (will be different from ApiUser)", example = DTOExamples.USER_ID)
    public UUID userId;


    @Schema(description = "if null, then use view_permission_id from twin or from twin_class", example = DTOExamples.PERMISSION_ID)
    public UUID permissionId;


}
