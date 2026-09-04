package org.twins.core.dto.rest.twinclass;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.featurer.FeaturerDTOv1;
import org.twins.core.dto.rest.related.RelatedObject;

import java.util.HashMap;
import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name = "TwinClassFieldValidatorV1")
public class TwinClassFieldValidatorDTOv1 {

    @Schema(description = "id")
    public UUID id;

    @Schema(description = "twin class field id")
    @RelatedObject(type = TwinClassFieldDTOv1.class, name = "twinClassField")
    public UUID twinClassFieldId;

    @Schema(description = "field validator featurer id")
    @RelatedObject(type = FeaturerDTOv1.class, name = "fieldValidatorFeaturer")
    public Integer fieldValidatorFeaturerId;

    @Schema(description = "field validator featurer params")
    public HashMap<String, String> fieldValidatorParams;

    @Schema(description = "backend validation error i18n id")
    public UUID beValidationErrorI18nId;

    @Schema(description = "backend validation error (resolved translation)")
    public String beValidationError;
}
