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
    //todo drop me when fix search v3
    protected int totalElements = 0;

    public static final SimplePagination SINGLE = new SimplePagination().setLimit(1).setOffset(0);
    public static final SimplePagination ALL = new SimplePagination().setLimit(Integer.MAX_VALUE).setOffset(0);
}