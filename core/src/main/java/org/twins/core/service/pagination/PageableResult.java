package org.twins.core.service.pagination;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
public class PageableResult<T> {
    private int offset;
    private int limit;
    private long total;
    private List<T> list;
}
