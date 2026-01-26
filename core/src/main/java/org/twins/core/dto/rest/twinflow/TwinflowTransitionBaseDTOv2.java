package org.twins.core.dto.rest.twinflow;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.DTOConfig;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.face.FaceDTOv1;
import org.twins.core.dto.rest.permission.PermissionDTOv1;
import org.twins.core.dto.rest.related.RelatedObject;
import org.twins.core.dto.rest.twinstatus.TwinStatusDTOv1;
import org.twins.core.dto.rest.user.UserDTOv1;
import org.twins.core.dto.rest.validator.TransitionValidatorRuleBaseDTOv1;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name =  "TwinflowTransitionBaseV2")
public class TwinflowTransitionBaseDTOv2 extends TwinflowTransitionBaseDTOv1 {
    @Schema(example = DTOExamples.TWIN_STATUS_ID)
    @RelatedObject(type = TwinStatusDTOv1.class, name = "srcTwinStatus")
    public UUID srcTwinStatusId;

    @Schema(example = DTOExamples.PERMISSION_ID)
    @RelatedObject(type = PermissionDTOv1.class, name = "permission")
    public UUID permissionId;

    @Schema(description = "twinflow id", example = DTOExamples.TWINFLOW_ID)
    @RelatedObject(type = TwinflowBaseDTOv1.class, name = "twinflow")
    public UUID twinflowId;

    @JsonFormat(pattern = DTOConfig.DATE_FORMAT)
    @Schema(description = "created at", example = DTOExamples.INSTANT)
    public LocalDateTime createdAt;

    @Schema(description = "createdByUserId")
    @RelatedObject(type = UserDTOv1.class, name = "createdByUser")
    public UUID createdByUserId;

    @Schema(description = "inbuilt factory id", example = DTOExamples.FACTORY_ID)
    @RelatedObject(type = FaceDTOv1.class, name = "inbuiltTwinFactory")
    public UUID inbuiltTwinFactoryId;

    @Schema(description = "drafting factory id", example = DTOExamples.FACTORY_ID)
    @RelatedObject(type = FaceDTOv1.class, name = "draftingTwinFactory")
    public UUID draftingTwinFactoryId;

    // moved from v3
    @Deprecated
    @Schema(description = "validators")
    public List<TransitionValidatorRuleBaseDTOv1> validatorRules;

    @Deprecated
    @Schema(description = "triggers")
    public List<TriggerDTOv1> triggers;
}


