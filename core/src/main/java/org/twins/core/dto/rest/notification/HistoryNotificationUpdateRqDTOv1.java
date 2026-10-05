package org.twins.core.dto.rest.notification;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.Request;

import java.util.List;

@EqualsAndHashCode(callSuper = false)
@Data
@Accessors(chain = true)
@Schema(name = "HistoryNotificationUpdateRequestV1")
public class HistoryNotificationUpdateRqDTOv1 extends Request {
    @Valid
    @Schema(description = "history notifications")
    public List<HistoryNotificationUpdateDTOv1> historyNotifications;
}
