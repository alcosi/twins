package org.twins.core.service.pagination;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
public class PaginationResult extends SimplePagination {
    protected long total;
}
