package org.twins.core.dto.rest.notification;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.pagination.PaginationDTOv1;

@EqualsAndHashCode(callSuper = false)
@Data
@Accessors(chain = true)
@Schema(name="HistoryNotificationRecipientSearchRsV1")
public class HistoryNotificationRecipientSearchRsDTOv1 extends HistoryNotificationRecipientListRsDTOv1 {
    @Schema(description = "pagination data")
    public PaginationDTOv1 pagination;
}
