package org.twins.core.dto.rest.factory;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.featurer.FeaturerDTOv1;
import org.twins.core.dto.rest.related.RelatedObject;

import java.util.Map;
import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name = "FactoryPipelineStepV1")
public class FactoryPipelineStepDTOv1 {
    @Schema(description = "id", example = DTOExamples.FACTORY_PIPELINE_STEP_ID)
    public UUID id;

    @Schema(description = "factory pipeline id", example = DTOExamples.FACTORY_PIPELINE_ID)
    @RelatedObject(type = FactoryPipelineDTOv1.class, name = "factoryPipeline")
    public UUID factoryPipelineId;

    @Schema(description = "order", example = "1")
    public Integer order;

    @Schema(description = "factory condition set id", example = DTOExamples.FACTORY_CONDITION_SET_ID)
    @RelatedObject(type = FactoryConditionSetDTOv1.class, name = "factoryConditionSet")
    public UUID factoryConditionSetId;

    @Schema(description = "factory condition invert", example = DTOExamples.BOOLEAN_TRUE)
    public Boolean factoryConditionInvert;

    @Schema(description = "is active", example = DTOExamples.BOOLEAN_TRUE)
    public Boolean active;

    @Schema(description = "is optional", example = DTOExamples.BOOLEAN_TRUE)
    public Boolean optional;

    @Schema(description = "filler featurer id", example = "22")
    @RelatedObject(type = FeaturerDTOv1.class, name = "fillerFeaturer")
    public Integer fillerFeaturerId;

    @Schema(description = "filler params", example = DTOExamples.FACTORY_PARAMS_MAP)
    public Map<String, String> fillerParams;

    @Schema(description = "description", example = DTOExamples.DESCRIPTION)
    public String description;
}


