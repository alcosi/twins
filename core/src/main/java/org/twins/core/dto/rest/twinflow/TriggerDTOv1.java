package org.twins.core.dto.rest.twinflow;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.trigger.TwinTriggerDTOv1;

import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@Schema(name = "TriggerV1")
public class TriggerDTOv1 extends TriggerBaseDTOv1 {
    @Schema(description = "id", example = DTOExamples.TRIGGER_ID)
    public UUID id;

    @Schema(description = "twin trigger")
    public TwinTriggerDTOv1 twinTrigger;
}
