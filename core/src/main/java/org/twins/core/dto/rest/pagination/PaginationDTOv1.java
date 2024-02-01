package org.twins.core.dto.rest.pagination;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@Schema(name = "PaginationV1")
public class PaginationDTOv1 {
    @Schema(description = "record number from which data sampling begins", example = "25")
    private int offset;
    @Schema(description = "number of records in the query result", example = "10")
    private int limit;
    @Schema(description = "total results count", example = "100")
    private long total;

    public PaginationDTOv1() {
    }
}
