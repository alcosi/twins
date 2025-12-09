package org.twins.core.dto.rest.factory;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.featurer.FeaturerDTOv1;
import org.twins.core.dto.rest.related.RelatedObject;

import java.util.HashMap;
import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name =  "FactoryConditionV1")
public class FactoryConditionDTOv1 {

    @Schema(description = "factory condition id", example = DTOExamples.FACTORY_ID)
    public UUID id;

    @Schema(description = "factory condition set id", example = DTOExamples.FACTORY_CONDITION_SET_ID)
    @RelatedObject(type = FactoryConditionSetDTOv1.class, name = "factoryConditionSet")
    public UUID factoryConditionSetId;

    @Schema(description = "conditioner feature id", example = DTOExamples.CONDITIONER_FEATURE_ID)
    @RelatedObject(type = FeaturerDTOv1.class, name = "conditionerFeaturer")
    public Integer conditionerFeaturerId;

    @Schema(description = "conditioner params", example = DTOExamples.FACTORY_PARAMS_MAP)
    public HashMap<String, String> conditionerParams;

    @Schema(description = "description", example = DTOExamples.DESCRIPTION)
    public String description;

    @Schema(description = "is active", example = DTOExamples.BOOLEAN_TRUE)
    public Boolean active;

    @Schema(description = "is invert", example = DTOExamples.BOOLEAN_TRUE)
    public Boolean invert;
}
