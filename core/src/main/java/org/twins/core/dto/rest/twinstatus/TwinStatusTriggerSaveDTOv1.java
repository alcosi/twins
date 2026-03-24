package org.twins.core.dto.rest.twinstatus;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.DTOExamples;

import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name = "TwinStatusTriggerSaveV1")
public class TwinStatusTriggerSaveDTOv1 {
    @Schema(description = "twin status id", example = DTOExamples.TWIN_STATUS_ID)
    public UUID twinStatusId;

    @Schema(description = "incoming else outgoing", example = DTOExamples.BOOLEAN_TRUE)
    public Boolean incomingElseOutgoing;

    @Schema(description = "order", example = DTOExamples.INTEGER)
    public Integer order;

    @Schema(description = "twin trigger id", example = DTOExamples.TRIGGER_ID)
    public UUID twinTriggerId;

    @Schema(description = "async", example = DTOExamples.BOOLEAN_TRUE)
    public Boolean async;

    @Schema(description = "active", example = DTOExamples.BOOLEAN_TRUE)
    public Boolean active;
}
