package org.twins.core.dto.rest.history;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.pagination.PaginationDTOv1;

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(fluent = true)
@Schema(name = "HistoryV1")
public class HistoryDTOv1 extends HistoryBaseDTOv2 {
    @Schema(description = "Detailed description for history item. Contains markdown")
    public String changeDescription;

    @Schema(description = "pagination data")
    public PaginationDTOv1 pagination;
}
