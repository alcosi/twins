package org.twins.core.dto.rest;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

@Schema
@Data
@Accessors(chain = true)
public class PaginationBean<K> {
    @Schema(description = "page number", example = "1")
    private int page;
    @Schema(description = "rows count per page", example = "10")
    private int count;
    @Schema(description = "total results count", example = "300")
    private K total;

    public PaginationBean() {
    }
}
