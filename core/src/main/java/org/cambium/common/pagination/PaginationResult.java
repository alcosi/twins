package org.cambium.common.pagination;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Collections;
import java.util.List;

@Data
@Accessors(chain = true)
public class PaginationResult<T> extends SimplePagination {
    public static final PaginationResult EMPTY = new PaginationResult()
            .setList(Collections.EMPTY_LIST)
            .setTotal(0);

    protected List<T> list;
    protected long total;
}
