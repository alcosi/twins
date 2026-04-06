package org.twins.core.dto.rest.trigger;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dao.trigger.TwinTriggerTaskStatus;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.featurer.FeaturerDTOv1;
import org.twins.core.dto.rest.related.RelatedObject;

import java.sql.Timestamp;
import java.util.Map;
import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name = "TwinTriggerTaskV1")
public class TwinTriggerTaskDTOv1 {
    @Schema(description = "id", example = DTOExamples.TRIGGER_ID)
    private UUID id;

    private UUID twinId;

    private UUID twinTriggerId;

    private UUID previousTwinStatusId;

    private UUID createdByUserId;

    private UUID businessAccountId;

    private TwinTriggerTaskStatus statusId;

    private String statusDetails;

    private Timestamp createdAt;

    private Timestamp doneAt;
}
