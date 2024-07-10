package org.twins.core.controller.rest.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static org.cambium.common.util.PaginationUtils.SORT_UNSORTED;

@Target(value = ElementType.PARAMETER)
@Retention(value = RetentionPolicy.RUNTIME)
public @interface SimplePaginationParams {
    int offset() default 0;
    int limit() default 10;
    boolean sortAsc() default true;
    String sortField() default SORT_UNSORTED;
}
