package org.twins.core.dto.rest.twinflow;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.cambium.i18n.dto.I18nDTOv1;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.Request;

import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(name =  "TransitionSaveRqV1")
public class TransitionSaveRqDTOv1 extends Request {

    @Schema(description = "I18n name", example = "")
    public I18nDTOv1 nameI18n;

    @Schema(description = "I18n description", example = "")
    public I18nDTOv1 descriptionI18n;

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

    @JsonIgnore
    public UUID twinflowId;

}
