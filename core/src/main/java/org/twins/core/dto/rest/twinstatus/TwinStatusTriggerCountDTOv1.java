package org.twins.core.dto.rest.twinstatus;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.CountDTOv1;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.related.RelatedObject;
import org.twins.core.dto.rest.trigger.TwinTriggerDTOv1;

import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
@Schema(name = "TwinStatusTriggerCountV1")
public class TwinStatusTriggerCountDTOv1 extends CountDTOv1 {
    @Schema(description = "twin status id", example = DTOExamples.TWIN_STATUS_ID)
    @RelatedObject(type = TwinStatusDTOv1.class, name = "twinStatus")
    public UUID twinStatusId;

    @Schema(description = "twin trigger id", example = DTOExamples.TRIGGER_ID)
    @RelatedObject(type = TwinTriggerDTOv1.class, name = "twinTrigger")
    public UUID twinTriggerId;

    @Schema(description = "is active")
    public Boolean active;

    @Schema(description = "is async")
    public Boolean async;

    @Schema(description = "incoming else outgoing")
    public Boolean incomingElseOutgoing;
}
