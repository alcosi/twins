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
@Schema(name = "HistoryNotificationRecipientCollectorCreateRqV1")
public class HistoryNotificationRecipientCollectorCreateRqDTOv1 extends Request {
    @Schema(description = "history notification recipient collectors")
    public List<HistoryNotificationRecipientCollectorCreateDTOv1> recipientCollectors;
}
