package org.twins.core.dto.rest.notification;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.UUID;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(name = "HistoryNotificationRecipientUpdateDTOv1")
public class HistoryNotificationRecipientUpdateDTOv1 extends HistoryNotificationRecipientSaveDTOv1 {
    @Schema(description = "history notification recipient id")
    public UUID id;
}
