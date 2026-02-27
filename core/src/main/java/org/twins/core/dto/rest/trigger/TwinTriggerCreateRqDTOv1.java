package org.twins.core.dto.rest.trigger;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.Request;

import java.util.List;

@Data
@Accessors(chain = true)
@Schema(name = "TwinTriggerCreateRqV1")
public class TwinTriggerCreateRqDTOv1 extends Request {
    @Schema(description = "trigger")
    public List<TwinTriggerCreateDTOv1> triggers;
}
