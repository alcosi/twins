package org.twins.core.controller.rest.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static org.cambium.common.util.PaginationUtils.*;

@Target(value = ElementType.PARAMETER)
@Retention(value = RetentionPolicy.RUNTIME)
public @interface SimplePaginationParams {
    int offset() default DEFAULT_VALUE_OFFSET;
    int limit() default DEFAULT_VALUE_LIMIT;
    boolean sortAsc() default true;
    String[] sortField() default SORT_UNSORTED;
}
