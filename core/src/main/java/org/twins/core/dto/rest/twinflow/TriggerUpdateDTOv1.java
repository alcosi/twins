package org.twins.core.dto.rest.twinflow;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@Schema(name = "TriggerUpdateV1")
public class TriggerUpdateDTOv1 extends TriggerDTOv1 {
}
