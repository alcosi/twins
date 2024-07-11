package org.cambium.common.pagination;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class SimplePagination {
    protected int offset;
    protected int limit;
    protected boolean sortAsc;
    protected String sortField;
}