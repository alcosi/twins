package org.twins.core.dto.rest.notification;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.twins.core.dto.rest.ResponseRelatedObjectsDTOv1;

import java.util.List;

@EqualsAndHashCode(callSuper = false)
@Data
@Schema(name = "HistoryNotificationRecipientListRsDTOv1")
public class HistoryNotificationRecipientListRsDTOv1 extends ResponseRelatedObjectsDTOv1 {
    @Schema(description = "history notification recipients")
    public List<HistoryNotificationRecipientDTOv1> recipients;
}
