package org.twins.core.dto.rest.twinflow;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.i18n.dto.I18nDTOv1;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.Request;

import java.util.UUID;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Schema(name =  "TwinflowSaveRqV1")
public class TwinflowSaveRqDTOv1 extends Request {

    @Schema(description = "I18n name", example = "")
    public I18nDTOv1 nameI18n;

    @Schema(description = "I18n description", example = "")
    public I18nDTOv1 descriptionI18n;

    @Schema(description = "initial status id", example = DTOExamples.TWIN_STATUS_ID)
    public UUID initialStatusId;

}
