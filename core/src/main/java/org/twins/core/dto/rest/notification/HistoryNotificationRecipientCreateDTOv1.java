package org.twins.core.dto.rest.notification;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Schema(name = "HistoryNotificationRecipientCreateV1")
public class HistoryNotificationRecipientCreateDTOv1 extends HistoryNotificationRecipientSaveDTOv1 {
}
