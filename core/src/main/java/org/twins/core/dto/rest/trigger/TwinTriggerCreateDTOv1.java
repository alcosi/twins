package org.twins.core.dto.rest.trigger;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@Schema(name = "TwinTriggerCreateV1")
public class TwinTriggerCreateDTOv1 extends TwinTriggerSaveDTOv1 {
}
