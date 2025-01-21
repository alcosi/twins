package org.twins.core.dto.rest.factory;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.ResponseRelatedObjectsDTOv1;
import org.twins.core.dto.rest.permission.FactoryPipelineStepDTOv2;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(name = "FactoryPipelineStepViewRsDTOv1")
public class FactoryPipelineStepViewRsDTOv1 extends ResponseRelatedObjectsDTOv1 {
    @Schema(description = "result - factory pipeline step")
    public FactoryPipelineStepDTOv2 step;
}
