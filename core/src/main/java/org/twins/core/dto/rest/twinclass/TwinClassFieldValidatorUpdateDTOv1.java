package org.twins.core.dto.rest.twinclass;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.i18n.I18nSaveDTOv1;

import java.util.HashMap;
import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name = "TwinClassFieldValidatorUpdateV1")
public class TwinClassFieldValidatorUpdateDTOv1 {

    @NotNull
    @Schema(description = "id", example = DTOExamples.UUID_ID)
    public UUID id;

    @Schema(description = "twin class field id this validator belongs to")
    public UUID twinClassFieldId;

    @Schema(description = "field validator featurer id")
    public Integer fieldValidatorFeaturerId;

    @Schema(description = "field validator featurer params")
    public HashMap<String, String> fieldValidatorParams;

    @Schema(description = "active")
    public Boolean active;

    @Schema(description = "backend validation error i18n")
    public I18nSaveDTOv1 beValidationErrorI18n;
}
