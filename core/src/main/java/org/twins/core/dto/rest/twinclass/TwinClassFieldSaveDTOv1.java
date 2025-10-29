package org.twins.core.dto.rest.twinclass;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.i18n.I18nSaveDTOv1;

import java.util.HashMap;
import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name = "TwinClassFieldSaveV1")
public abstract class TwinClassFieldSaveDTOv1{

    @Schema(description = "unique key within the class", example = DTOExamples.TWIN_CLASS_FIELD_KEY)
    public String key;

    @Schema(description = "[optional] this field helps to set extra permission, needed by users to view this field", example = "")
    public UUID viewPermissionId;

    @Schema(description = "[optional] this field helps to set extra permission, needed by users to edit this field", example = "")
    public UUID editPermissionId;

    @Schema(description = "I18n name", example = "")
    public I18nSaveDTOv1 nameI18n;

    @Schema(description = "I18n description", example = "")
    public I18nSaveDTOv1 descriptionI18n;

    @Schema(description = "Required field", example = "true")
    public Boolean required;

    @Schema(description = "System field", example = "true")
    public Boolean system;

    @Schema(description = "Is dependent field", example = "true")
    public Boolean dependentField;

    @Schema(description = "has dependent fields", example = "true")
    public Boolean hasDependentFields;

    @Schema(description = "external id", example = "")
    public String externalId;

    @Schema(description = "Field typer featurer ID", example = "1")
    public Integer fieldTyperFeaturerId;

    @Schema(description = "Field typer parameters", example = "{}")
    public HashMap<String, String> fieldTyperParams;

    @Schema(description = "I18n frontend validation error", example = "")
    public I18nSaveDTOv1 feValidationErrorI18n;

    @Schema(description = "I18n backend validation error", example = "")
    public I18nSaveDTOv1 beValidationErrorI18n;

    @Schema(description = "order", example = "1")
    public Integer order;
}
