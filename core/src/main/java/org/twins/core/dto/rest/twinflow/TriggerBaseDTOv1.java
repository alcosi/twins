package org.twins.core.dto.rest.twinflow;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.DTOExamples;

import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name = "TriggerBaseV1")
public class TriggerBaseDTOv1 {

    @Schema(description = "order")
    public Integer order;

    @Schema(description = "twin trigger id", example = DTOExamples.TRIGGER_ID)
    public UUID twinTriggerId;

    @Schema(description = "async", example = DTOExamples.BOOLEAN_TRUE)
    public Boolean async;

    @Schema(description = "active", example = DTOExamples.BOOLEAN_TRUE)
    public Boolean active;
}
