package org.twins.core.dto.rest.factory;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.Request;

import java.util.List;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(name = "FactoryPipelineUpdateRqV1")
public class FactoryPipelineUpdateRqDTOv1 extends Request {
    @Valid
    @Schema(description = "factory pipeline update list")
    public List<FactoryPipelineUpdateDTOv1> factoryPipelines;
}
