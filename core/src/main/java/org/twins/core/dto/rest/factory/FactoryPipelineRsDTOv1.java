package org.twins.core.dto.rest.factory;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.ResponseRelatedObjectsDTOv1;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(name = "FactoryPipelineRsV1")
public class FactoryPipelineRsDTOv1 extends ResponseRelatedObjectsDTOv1 {
    @Schema(description = "results - factory pipeline")
    public FactoryPipelineDTOv1 factoryPipeline;
}