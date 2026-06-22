package org.twins.core.dto.rest.factory;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name = "FactoryPipelineStepDuplicateV1")
public class FactoryPipelineStepDuplicateDTOv1 {
    @Schema(description = "original factory pipeline step id")
    public UUID originalFactoryPipelineStepId;

    @Schema(description = "[optional] fill if step should be copied to other pipeline")
    public UUID newTwinFactoryPipelineId;
}
