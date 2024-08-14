package org.twins.core.dto.rest.twinflow;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@Schema(name = "TriggerCreateV1")
public class TriggerCreateDTOv1 extends TriggerBaseDTOv1 {
}
