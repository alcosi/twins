package org.twins.core.dto.rest.twinstatus;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.DTOExamples;

import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name = "TwinStatusTriggerCreateV1")
public class TwinStatusTriggerCreateDTOv1 {
    @NotNull
    @Schema(description = "twin status id", example = DTOExamples.TWIN_STATUS_ID)
    public UUID twinStatusId;

    @NotNull
    @Schema(description = "incoming else outgoing", example = DTOExamples.BOOLEAN_TRUE)
    public Boolean incomingElseOutgoing;

    @Schema(description = "order", example = DTOExamples.INTEGER)
    public Integer order;

    @NotNull
    @Schema(description = "twin trigger id", example = DTOExamples.TRIGGER_ID)
    public UUID twinTriggerId;

    @Schema(description = "async", example = DTOExamples.BOOLEAN_TRUE)
    public Boolean async;

    @Schema(description = "active", example = DTOExamples.BOOLEAN_TRUE)
    public Boolean active;
}
