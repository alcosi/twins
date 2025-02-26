package org.twins.core.dto.rest.factory;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.Request;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(name = "FactoryPipelineStepCreateRqV1")
public class FactoryPipelineStepCreateRqDTOv1 extends Request {
    @Schema(description = "factory pipeline step create")
    public FactoryPipelineStepCreateDTOv1 factoryPipelineStep;
}
