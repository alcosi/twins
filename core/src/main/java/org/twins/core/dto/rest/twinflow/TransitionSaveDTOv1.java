package org.twins.core.dto.rest.twinflow;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.i18n.I18nSaveDTOv1;
import org.twins.core.enums.twinflow.TwinflowTransitionType;

import java.util.UUID;

@Data
@Schema(name =  "TransitionSaveV1")
public class TransitionSaveDTOv1 {
    @Schema(description = "I18n name", example = "")
    public I18nSaveDTOv1 nameI18n;

    @Schema(description = "I18n description", example = "")
    public I18nSaveDTOv1 descriptionI18n;

    @Schema(description = "[optional] src status id. if null - from any status transition", example = DTOExamples.TWIN_STATUS_ID)
    public UUID srcStatusId;

    @Schema(description = "dst status is required", example = DTOExamples.TWIN_STATUS_ID)
    public UUID dstStatusId;

    @Schema(description = "[optional] uniq alias inside twinflow", example = DTOExamples.TWINFLOW_TRANSITION_ALIAS)
    public String alias;

    @Schema(description = "[optional] some permission required to run transition", example = DTOExamples.PERMISSION_ID)
    public UUID permissionId;

    @Schema(description = "Inbuilt TwinFactory Id", example = "")
    public UUID inbuiltTwinFactoryId;

    @Schema(description = "Drafting TwinFactory Id", example = "")
    public UUID draftingTwinFactoryId;

    @Schema(description = "twinflow transition type id", example = DTOExamples.TWINFLOW_TRANSITION_TYPE_ID)
    public TwinflowTransitionType twinflowTransitionTypeId;

    @JsonIgnore
    public UUID twinflowId;

}
