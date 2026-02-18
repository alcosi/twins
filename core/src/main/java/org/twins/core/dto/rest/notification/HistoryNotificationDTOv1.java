package org.twins.core.dto.rest.notification;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.related.RelatedObject;
import org.twins.core.dto.rest.twinclass.TwinClassDTOv1;
import org.twins.core.dto.rest.twinclass.TwinClassFieldDTOv1;
import org.twins.core.dto.rest.validator.TwinValidatorSetDTOv1;

import java.sql.Timestamp;
import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name = "HistoryNotificationV1")
public class HistoryNotificationDTOv1 {
    @Schema(description = "id")
    public UUID id;

    @Schema(description = "history type id")
    public String historyTypeId;

    @Schema(description = "twin class id", example = DTOExamples.TWIN_CLASS_ID)
    @RelatedObject(type = TwinClassDTOv1.class, name = "twinClass")
    public UUID twinClassId;

    @Schema(description = "twin class field id", example = DTOExamples.TWIN_CLASS_FIELD_ID)
    @RelatedObject(type = TwinClassFieldDTOv1.class, name = "twinClassField")
    public UUID twinClassFieldId;

    @Schema(description = "twin validator set id")
    @RelatedObject(type = TwinValidatorSetDTOv1.class, name = "twinValidatorSet")
    public UUID twinValidatorSetId;

    @Schema(description = "twin validator set invert")
    public Boolean twinValidatorSetInvert;

    @Schema(description = "notification schema id")
    public UUID notificationSchemaId;

    @Schema(description = "history notification recipient id")
    @RelatedObject(type = HistoryNotificationRecipientDTOv1.class, name = "historyNotificationRecipient")
    public UUID historyNotificationRecipientId;

    @Schema(description = "notification channel event id")
    public UUID notificationChannelEventId;

    @Schema(description = "created by user id")
    public UUID createdByUserId;

    @Schema(description = "created at")
    public Timestamp createdAt;
}
