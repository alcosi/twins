package org.twins.core.dto.rest.trigger;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.CountDTOv1;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.businessaccount.BusinessAccountDTOv1;
import org.twins.core.dto.rest.related.RelatedObject;
import org.twins.core.dto.rest.twin.TwinDTOv2;
import org.twins.core.dto.rest.twinstatus.TwinStatusDTOv1;
import org.twins.core.dto.rest.user.UserDTOv1;
import org.twins.core.enums.trigger.TwinTriggerTaskStatus;

import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
@Schema(name = "TwinTriggerTaskCountV1")
public class TwinTriggerTaskCountDTOv1 extends CountDTOv1 {
    @Schema(description = "twin id", example = DTOExamples.TWIN_ID)
    @RelatedObject(type = TwinDTOv2.class, name = "twin")
    public UUID twinId;

    @Schema(description = "twin trigger id", example = DTOExamples.TRIGGER_ID)
    @RelatedObject(type = TwinTriggerDTOv1.class, name = "twinTrigger")
    public UUID twinTriggerId;

    @Schema(description = "previous twin status id", example = DTOExamples.TWIN_STATUS_ID)
    @RelatedObject(type = TwinStatusDTOv1.class, name = "previousTwinStatus")
    public UUID previousTwinStatusId;

    @Schema(description = "created by user id", example = DTOExamples.USER_ID)
    @RelatedObject(type = UserDTOv1.class, name = "createdByUser")
    public UUID createdByUserId;

    @Schema(description = "business account id", example = DTOExamples.BUSINESS_ACCOUNT_ID)
    @RelatedObject(type = BusinessAccountDTOv1.class, name = "businessAccount")
    public UUID businessAccountId;

    @Schema(description = "twin trigger task status")
    public TwinTriggerTaskStatus statusId;
}
