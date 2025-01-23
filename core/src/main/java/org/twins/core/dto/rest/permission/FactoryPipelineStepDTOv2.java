package org.twins.core.dto.rest.permission;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.factory.FactoryConditionSetDTOv1;
import org.twins.core.dto.rest.factory.FactoryPipelineDTOv2;
import org.twins.core.dto.rest.featurer.FeaturerDTOv1;

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@Schema(name =  "FactoryPipelineStepV2")
public class FactoryPipelineStepDTOv2 extends FactoryPipelineStepDTOv1 {
    @Schema(description = "factory pipeline")
    public FactoryPipelineDTOv2 factoryPipeline;

    @Schema(description = "factory condition set")
    public FactoryConditionSetDTOv1 factoryConditionSet;

    @Schema(description = "filler featurer")
    public FeaturerDTOv1 fillerFeaturer;
}
