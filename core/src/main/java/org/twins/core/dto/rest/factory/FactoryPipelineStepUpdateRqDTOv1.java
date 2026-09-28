package org.twins.core.dto.rest.factory;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.Request;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(name = "FactoryPipelineStepUpdateRqV1")
public class FactoryPipelineStepUpdateRqDTOv1 extends Request {
    @Valid
    @Schema(description = "factory pipeline step update")
    public FactoryPipelineStepUpdateDTOv1 factoryPipelineStep;
}

