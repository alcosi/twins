package org.twins.core.dto.rest.notification;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@Schema(name = "HistoryNotificationRecipientCollectorCreateV1")
public class HistoryNotificationRecipientCollectorCreateDTOv1 extends HistoryNotificationRecipientCollectorSaveDTOv1 {
}
