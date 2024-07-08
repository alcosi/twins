package org.cambium.common.util;

import org.cambium.common.exception.ServiceException;
import org.cambium.common.pagination.PaginationResult;
import org.springframework.data.domain.*;
import org.cambium.common.pagination.SimplePagination;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

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

    public static <T> PaginationResult<T> convertInPaginationResult(SimplePagination pagination) throws ServiceException {
        Page<T> emptyPage = new PageImpl<>(new ArrayList<>(), pageableOffset(pagination), 0);
        return convertInPaginationResult(emptyPage, pagination);
    }

    public static <T> PaginationResult<T> convertInPaginationResult(Page<T> page, SimplePagination pagination) {
        PaginationResult<T> result = new PaginationResult<>();
        result
            .setList(page.getContent())
            .setTotal(page.getTotalElements())
            .setOffset(pagination.getOffset())
            .setLimit(pagination.getLimit());
        return result;
    }

    public static <T> PaginationResult<T> convertInPaginationResult(Page<T> page, SimplePagination pagination, Function<T, Boolean> filterFunction) {
        PaginationResult<T> result = new PaginationResult<>();
        result
            .setList(page.getContent().stream().filter(filterFunction::apply).toList())
            .setTotal(page.getTotalElements())
            .setOffset(pagination.getOffset())
            .setLimit(pagination.getLimit());
        return result;
    }

    public static <T> PaginationResult<T> convertInPaginationResult(List<T> list, SimplePagination pagination, long total) throws ServiceException {
        Page<T> page = new PageImpl<>(list, pageableOffset(pagination), total);
        return convertInPaginationResult(page, pagination);
    }

}
