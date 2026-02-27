package org.twins.core.dto.rest.twinstatus;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dao.twin.TwinStatusTransitionTriggerEntity;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.related.RelatedObject;
import org.twins.core.dto.rest.twinstatus.TwinStatusDTOv1;
import org.twins.core.dto.rest.trigger.TwinTriggerDTOv1;

import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name = "TwinStatusTransitionTriggerSaveV1")
public class TwinStatusTransitionTriggerSaveDTOv1 {
    @Schema(description = "twin status id", example = DTOExamples.TWIN_STATUS_ID)
    @RelatedObject(type = TwinStatusDTOv1.class, name = "twinStatus")
    public UUID twinStatusId;

    @Schema(description = "transition type", example = "incoming")
    public TwinStatusTransitionTriggerEntity.TransitionType type;

    @Schema(description = "order", example = DTOExamples.INTEGER)
    public Integer order;

    @Schema(description = "twin trigger id", example = DTOExamples.TRIGGER_ID)
    @RelatedObject(type = TwinTriggerDTOv1.class, name = "twinTrigger")
    public UUID twinTriggerId;

    @Schema(description = "async", example = DTOExamples.BOOLEAN_TRUE)
    public Boolean async;

    @Schema(description = "active", example = DTOExamples.BOOLEAN_TRUE)
    public Boolean active;
}
