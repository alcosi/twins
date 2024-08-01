package org.cambium.common.pagination;

import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;

@Data
@Accessors(chain = true)
@FieldNameConstants
public class SimplePagination {
    protected int offset;
    protected int limit;
    protected boolean sortAsc;
    protected String sortField;
}