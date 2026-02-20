package org.twins.core.dto.rest.twinflow;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.DTOExamples;

import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@Schema(name = "TransitionUpdateV1")
public class TransitionUpdateDTOv1 extends TransitionSaveDTOv1 {
    @Schema(description = "transition id", example = DTOExamples.TWINFLOW_TRANSITION_ID)
    public UUID id;
}
