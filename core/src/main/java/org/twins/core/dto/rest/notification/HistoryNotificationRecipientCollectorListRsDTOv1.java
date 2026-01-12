package org.twins.core.dto.rest.notification;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.ResponseRelatedObjectsDTOv1;

import java.util.List;

@EqualsAndHashCode(callSuper = false)
@Data
@Schema(name = "HistoryNotificationRecipientCollectorListRsV1")
@Accessors(chain = true)
public class HistoryNotificationRecipientCollectorListRsDTOv1 extends ResponseRelatedObjectsDTOv1 {
    @Schema(description = "history notification recipient collectors")
    public List<HistoryNotificationRecipientCollectorDTOv1> recipientCollectors;
}
