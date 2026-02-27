package org.twins.core.dto.rest.twinstatus;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@Schema(name = "TwinStatusTransitionTriggerUpdateV1")
public class TwinStatusTransitionTriggerUpdateDTOv1 extends TwinStatusTransitionTriggerSaveDTOv1 {
    @Schema(description = "id")
    public UUID id;
}
