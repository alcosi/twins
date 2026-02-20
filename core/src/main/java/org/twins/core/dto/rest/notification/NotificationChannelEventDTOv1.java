package org.twins.core.dto.rest.notification;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.related.RelatedObject;

import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name = "NotificationChannelEventV1")
public class NotificationChannelEventDTOv1 {
    @Schema(description = "id", example = DTOExamples.UUID_ID)
    public UUID id;

    @RelatedObject(type = NotificationChannelDTOv1.class, name = "notificationChannel")
    @Schema(description = "notification channel id", example = DTOExamples.UUID_ID)
    public UUID notificationChannelId;

    @Schema(description = "event code")
    public String eventCode;

    @RelatedObject(type = NotificationContextDTOv1.class, name = "notificationContext")
    @Schema(description = "notification context id", example = DTOExamples.UUID_ID)
    public UUID notificationContextId;

    @Schema(description = "unique in batch")
    public boolean uniqueInBatch;
}
