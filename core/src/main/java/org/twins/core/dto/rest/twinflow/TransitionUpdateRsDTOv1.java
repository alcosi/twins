package org.twins.core.dto.rest.twinflow;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.Response;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Schema(name = "TwinflowTransitionUpdateRsV1")
public class TransitionUpdateRsDTOv1 extends Response {
    @Schema(description = "result - twinflow transition")
    public TwinflowTransitionBaseDTOv2 transition;
}
