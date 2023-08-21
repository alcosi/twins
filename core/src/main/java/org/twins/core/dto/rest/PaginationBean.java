package org.twins.core.dto.rest;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel
public class PaginationBean {
    @ApiModelProperty(notes = "page number", example = "1")
    private int page;
    @ApiModelProperty(notes = "rows count per page", example = "10")
    private int count;
    @ApiModelProperty(notes = "total results count", example = "300")
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
