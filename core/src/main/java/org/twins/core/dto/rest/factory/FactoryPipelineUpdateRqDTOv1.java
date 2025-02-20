package org.twins.core.dto.rest.factory;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.Request;

import java.util.UUID;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(name = "FactoryPipelineUpdateRqV1")
public class FactoryPipelineUpdateRqDTOv1 extends Request {
    @Schema(description = "factory pipeline update")
    public FactoryPipelineUpdateDTOv1 factoryPipelineUpdateDTO;

}
