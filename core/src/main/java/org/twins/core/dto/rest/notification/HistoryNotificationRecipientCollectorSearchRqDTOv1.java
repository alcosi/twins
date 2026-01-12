package org.twins.core.dto.rest.notification;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.LongRangeDTOv1;
import org.twins.core.dto.rest.Request;

import java.util.Set;
import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Schema(name = "HistoryNotificationRecipientCollectorSearchRqV1")
public class HistoryNotificationRecipientCollectorSearchRqDTOv1 extends Request {
    @Schema
    public HistoryNotificationRecipientCollectorSearchDTOv1 collector;
}
