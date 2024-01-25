package org.twins.core.dto.rest.pagination;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@Schema(name = "PaginationV1")
public class PaginationDTOv1 {
    @Schema(description = "page number", example = "1")
    private int page;
    @Schema(description = "rows count per page", example = "10")
    private int count;
    @Schema(description = "total results count", example = "300")
    private long total;

    public PaginationDTOv1() {
    }
}
