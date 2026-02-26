package org.twins.core.dto.rest.notification;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.DTOExamples;

import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name = "HistoryNotificationSaveV1")
public class HistoryNotificationSaveDTOv1 {
    @Schema(description = "history type id")
    public String historyTypeId;

    @Schema(description = "twin class id", example = DTOExamples.TWIN_CLASS_ID)
    public UUID twinClassId;

    @Schema(description = "twin class field id", example = DTOExamples.TWIN_CLASS_FIELD_ID)
    public UUID twinClassFieldId;

    @Schema(description = "twin validator set id")
    public UUID twinValidatorSetId;

    @Schema(description = "twin validator set invert")
    public Boolean twinValidatorSetInvert;

    @Schema(description = "notification schema id")
    public UUID notificationSchemaId;

    @Schema(description = "history notification recipient id")
    public UUID historyNotificationRecipientId;

    @Schema(description = "notification channel event id")
    public UUID notificationChannelEventId;
}
