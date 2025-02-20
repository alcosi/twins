package org.twins.core.dto.rest.factory;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.UUID;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Schema(name = "FactoryPipelineUpdateV1")
public class FactoryPipelineUpdateDTOv1 extends FactoryPipelineSaveDTOv1 {
    @JsonIgnore
    public UUID pipelineId;
}
