package org.cambium.common.pagination;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
public class PaginationResult<T> extends SimplePagination {
    protected List<T> list;
    protected long total;
}
