package org.twins.core.dto.rest.notification;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.ResponseRelatedObjectsDTOv1;

import java.util.List;

@EqualsAndHashCode(callSuper = false)
@Data
@Accessors(chain = true)
@Schema(name = "HistoryNotificationListResponseV1")
public class HistoryNotificationListRsDTOv1 extends ResponseRelatedObjectsDTOv1 {
    @Schema(description = "history notifications")
    public List<HistoryNotificationDTOv1> historyNotifications;
}
