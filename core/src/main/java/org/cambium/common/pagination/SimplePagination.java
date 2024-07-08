package org.cambium.common.pagination;

import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.data.domain.Sort;

@Data
@Accessors(chain = true)
public class SimplePagination {
    protected int offset;
    protected int limit;
    protected Sort sort;
}