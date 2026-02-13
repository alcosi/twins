package org.twins.core.dto.rest.notification;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@Schema(name = "HistoryNotificationRecipientCollectorUpdateV1")
public class HistoryNotificationRecipientCollectorUpdateDTOv1 extends HistoryNotificationRecipientCollectorSaveDTOv1 {
    @Schema(description = "history notification recipient collector id")
    public UUID id;
}
