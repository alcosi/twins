package org.twins.core.dto.rest.transition;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Schema(name = "TransitionTriggerCreateV1")
public class TransitionTriggerCreateDTOv1 extends TransitionTriggerSaveDTOv1 {
}
