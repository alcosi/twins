package org.twins.core.dto.rest.twinclass;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.CountDTOv1;
import org.twins.core.dto.rest.featurer.FeaturerDTOv1;
import org.twins.core.dto.rest.related.RelatedObject;

import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
@Schema(name = "TwinClassFieldValidatorCountV1")
public class TwinClassFieldValidatorCountDTOv1 extends CountDTOv1 {
    @Schema(description = "twin class field id")
    @RelatedObject(type = TwinClassFieldDTOv1.class, name = "twinClassField")
    public UUID twinClassFieldId;

    @Schema(description = "field validator featurer id")
    @RelatedObject(type = FeaturerDTOv1.class, name = "fieldValidatorFeaturer")
    public Integer fieldValidatorFeaturerId;
}
