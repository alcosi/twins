package org.twins.core.dto.rest.notification;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.Request;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Schema(name = "HistoryNotificationRecipientCreateRqDTOv1")
public class HistoryNotificationRecipientCreateRqDTOv1 extends Request {
    @Schema(description = "history notification recipients")
    public List<HistoryNotificationRecipientCreateDTOv1> recipients;
}
