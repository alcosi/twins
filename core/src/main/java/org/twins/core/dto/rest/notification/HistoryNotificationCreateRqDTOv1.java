package org.twins.core.dto.rest.notification;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.Request;

import java.util.List;

@EqualsAndHashCode(callSuper = false)
@Data
@Accessors(chain = true)
@Schema(name = "HistoryNotificationCreateRqV1")
public class HistoryNotificationCreateRqDTOv1 extends Request {
    @Schema(description = "history notifications")
    public List<HistoryNotificationCreateDTOv1> historyNotifications;
}
