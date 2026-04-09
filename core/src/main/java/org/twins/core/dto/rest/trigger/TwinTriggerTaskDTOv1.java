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
    @Schema(description = "id")
    public UUID id;

    @Schema(description = "twin Id", example = DTOExamples.TWIN_ID)
    public UUID twinId;

    @Schema(description = "twin trigger Id", example = DTOExamples.TRIGGER_ID)
    public UUID twinTriggerId;

    @Schema(description = "previous twin status Id", example = DTOExamples.TWIN_STATUS_ID)
    public UUID previousTwinStatusId;

    @Schema(description = "created by user Id", example = DTOExamples.USER_ID)
    public UUID createdByUserId;

    @Schema(description = "businessAccount Id", example = DTOExamples.BUSINESS_ACCOUNT_ID)
    public UUID businessAccountId;

    @Schema(description = "twin trigger task status")
    public TwinTriggerTaskStatus statusId;

    @Schema(description = "status details")
    public String statusDetails;

    @Schema(description = "created at")
    public Timestamp createdAt;

    @Schema(description = "done at")
    public Timestamp doneAt;
}
