package org.twins.core.dto.rest.twinflow;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.DTOConfig;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.permission.PermissionDTOv1;
import org.twins.core.dto.rest.twinstatus.TwinStatusDTOv1;
import org.twins.core.dto.rest.user.UserDTOv1;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name =  "TwinflowTransitionBaseV2")
public class TwinflowTransitionBaseDTOv2 extends TwinflowTransitionBaseDTOv1 {
    @Schema(example = DTOExamples.TWIN_STATUS_ID)
    public UUID srcTwinStatusId;

    @Schema(description = "srcStatus")
    public TwinStatusDTOv1 srcTwinStatus;

    @Schema(example = DTOExamples.PERMISSION_ID)
    public UUID permissionId;

    @Schema(description = "permission details")
    public PermissionDTOv1 permission;

    @JsonFormat(pattern = DTOConfig.DATE_FORMAT)
    @Schema(description = "created at", example = DTOExamples.INSTANT)
    public LocalDateTime createdAt;

    @Schema(description = "twinflow author")
    public UserDTOv1 createdByUser;

    @Schema(description = "createdByUserId")
    public UUID createdByUserId;
}
