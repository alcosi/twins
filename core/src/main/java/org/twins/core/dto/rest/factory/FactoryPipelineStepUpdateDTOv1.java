package org.twins.core.dto.rest.factory;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Schema(name = "FactoryPipelineStepUpdateV1")
public class FactoryPipelineStepUpdateDTOv1 extends FactoryPipelineStepSaveDTOv1 {
}
