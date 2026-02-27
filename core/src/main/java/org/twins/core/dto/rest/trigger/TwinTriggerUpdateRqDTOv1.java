package org.twins.core.dto.rest.trigger;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.Request;

import java.util.List;

@Data
@Accessors(chain = true)
@Schema(name = "TwinTriggerUpdateV1")
public class TwinTriggerUpdateRqDTOv1 extends Request {
    @Schema(description = "trigger")
    public List<TwinTriggerUpdateDTOv1> triggers;
}
