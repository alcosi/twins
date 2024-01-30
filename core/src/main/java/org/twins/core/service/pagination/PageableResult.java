package org.twins.core.service.pagination;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public abstract class PageableResult {
    private int page;
    private int count;
    private long total;
}
