package org.twins.core.dto.rest.twinflow;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.i18n.I18nHasTranslation;
import org.twins.core.dto.rest.i18n.I18nSaveDTOv1;

import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name = "TwinflowCreateV1")
public class TwinflowCreateDTOv1 {

    @NotNull
    @Schema(description = "twin class id", example = DTOExamples.TWIN_CLASS_ID)
    public UUID twinClassId;

    @NotNull
    @I18nHasTranslation
    @Schema(description = "I18n name", example = "")
    public I18nSaveDTOv1 nameI18n;

    @Schema(description = "I18n description", example = "")
    public I18nSaveDTOv1 descriptionI18n;

    @NotNull
    @Schema(description = "initial status id", example = DTOExamples.TWIN_STATUS_ID)
    public UUID initialStatusId;

    @Schema(description = "initial sketch status id", example = DTOExamples.TWIN_STATUS_ID)
    public UUID initialSketchStatusId;

    @Schema(description = "inheritable")
    public Boolean inheritable;
}
