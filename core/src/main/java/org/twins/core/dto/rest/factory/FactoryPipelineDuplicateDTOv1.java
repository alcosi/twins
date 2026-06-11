package org.twins.core.dto.rest.factory;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name = "FactoryPipelineDuplicateV1")
public class FactoryPipelineDuplicateDTOv1 {
    @Schema(description = "original factory pipeline id")
    public UUID originalFactoryPipelineId;

    @Schema(description = "[optional] fill if pipeline should be copied to other factory")
    public UUID newTwinFactoryId;
}
