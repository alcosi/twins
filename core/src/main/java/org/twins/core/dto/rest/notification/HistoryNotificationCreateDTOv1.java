package org.twins.core.dto.rest.notification;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
@Schema(name = "HistoryNotificationCreateV1")
public class HistoryNotificationCreateDTOv1 extends HistoryNotificationSaveDTOv1 {
}
