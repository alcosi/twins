package org.twins.core.service.pagination;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public abstract class PageableResult {
    private int offset;
    private int limit;
    private long total;
}
