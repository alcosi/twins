package org.twins.core.dto.rest.twinflow;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.Request;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Schema(name = "TransitionUpdateRqV1")
public class TransitionUpdateRqDTOv1 extends Request {
    @Schema(description = "transition")
    public TransitionUpdateDTOv1 transition;
}
