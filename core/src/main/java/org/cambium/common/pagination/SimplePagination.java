package org.cambium.common.pagination;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.cambium.common.util.PaginationUtils;

@Data
@Accessors(chain = true)
public class SimplePagination {
    @Schema(example = PaginationUtils.DEFAULT_VALUE_OFFSET)
    protected int offset;
    @Schema(example = PaginationUtils.DEFAULT_VALUE_LIMIT)
    protected int limit;
    //todo
    @Schema(example = "false")
    protected boolean sortAsc;
    //todo just only information if dont touch will be createAt
//    @Schema(description = "by default unsorted, if annotation parameter is empty or this sortField is missing" ,example = "createAt")
    @Schema(example = "createAt")
    protected String sortField;
}