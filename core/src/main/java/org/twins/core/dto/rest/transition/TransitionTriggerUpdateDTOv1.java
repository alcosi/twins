package org.twins.core.dto.rest.transition;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Schema(name = "TransitionTriggerUpdateV1")
public class TransitionTriggerUpdateDTOv1 extends TransitionTriggerSaveDTOv1 {
}
