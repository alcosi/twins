package org.twins.core.dto.rest.twinflow;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Schema(name =  "TransitionUpdateRqV1")
public class TransitionUpdateRqDTOv1 extends TransitionSaveRqDTOv1 {
    @Schema(description = "validators cud operations")
    public ValidatorCudDTOv1 validators;

    @Schema(description = "triggers cud operations")
    public TriggerCudDTOv1 triggers;
}
