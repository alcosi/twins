package org.twins.core.dto.rest;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema
public class PaginationBean {
    @Schema(description = "page number", example = "1")
    private int page;
    @Schema(description = "rows count per page", example = "10")
    private int count;
    @Schema(description = "total results count", example = "300")
    private int total;

    public PaginationBean() {
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }
}
