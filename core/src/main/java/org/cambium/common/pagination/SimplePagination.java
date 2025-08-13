package org.cambium.common.pagination;

import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import org.springframework.data.domain.Sort;

@Data
@Accessors(chain = true)
@FieldNameConstants
public class SimplePagination {
    public static final String SORT_FIELD = "sortField";
    public static final String SORT_ASC = "sortAsc";
    protected int offset;
    protected int limit;
    protected Sort sort;
    //todo drop me when fix search v3
    protected int totalElements = 0;

    public static final SimplePagination SINGLE = new SimplePagination().setLimit(1).setOffset(0);
    public static final SimplePagination FRIENDLY = new SimplePagination().setLimit(100).setOffset(0);
    public static final SimplePagination ALL = new SimplePagination().setLimit(Integer.MAX_VALUE).setOffset(0);
}