package org.cambium.common.util;

import org.cambium.common.exception.ServiceException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.twins.core.service.pagination.PaginationResult;
import org.twins.core.service.pagination.SimplePagination;

import static org.twins.core.exception.ErrorCodeTwins.PAGINATION_ERROR;

public class PaginationUtils {
    public static final String DEFAULT_VALUE_LIMIT = "10";
    public static final String DEFAULT_VALUE_OFFSET = "0";

    public static Sort sort(boolean asc, String field) {
        Sort.Direction direction = Sort.Direction.DESC;
        if (asc) direction = Sort.Direction.ASC;
        return Sort.by(direction, field);
    }

    public static Pageable pagination(int page, int size) {
        return PageRequest.of(page, size);
    }

    public static Pageable pagination(int page, int size, Sort sort) {
        return PageRequest.of(page, size, sort);
    }

    public static SimplePagination convertPagableInSimplePagination(Pageable pageable) {
        return new SimplePagination().setOffset((int) pageable.getOffset()).setLimit(pageable.getPageSize());
    }

    public static Pageable pageableOffset(SimplePagination pagination) throws ServiceException {
        if (pagination.getOffset() % pagination.getLimit() > 0) throw new ServiceException(PAGINATION_ERROR);
        return pagination.getSort() == null
                ? PageRequest.of(pagination.getOffset() / pagination.getLimit(), pagination.getLimit())
                : PageRequest.of(pagination.getOffset() / pagination.getLimit(), pagination.getLimit(), pagination.getSort());
    }

    public static SimplePagination createSimplePagination(int offset, int limit, Sort sort) {
        return new SimplePagination()
                .setOffset(offset)
                .setLimit(limit)
                .setSort(sort);
    }

}
