package org.cambium.common.util;

import org.cambium.common.exception.ServiceException;
import org.cambium.common.pagination.PaginationResult;
import org.cambium.common.pagination.SimplePagination;
import org.springframework.data.domain.*;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import static org.twins.core.exception.ErrorCodeTwins.PAGINATION_ERROR;
import static org.twins.core.exception.ErrorCodeTwins.PAGINATION_LIMIT_ERROR;

public class PaginationUtils {
    public static final int DEFAULT_VALUE_OFFSET = 0;
    public static final int DEFAULT_VALUE_LIMIT = 10;
    public static final String SORT_UNSORTED = "unsorted";

    public static Sort sortType(boolean sortAsc, String... sortField) {
        if (sortField == null || sortField.length == 0 || (sortField.length == 1 && (sortField[0].equals(SORT_UNSORTED) || StringUtils.isEmpty(sortField[0])))) {
            return Sort.unsorted();
        }
        return Sort.by(sortAsc ? Sort.Direction.ASC : Sort.Direction.DESC, sortField);
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
        if (pagination.getLimit() < 1)
            throw new ServiceException(PAGINATION_LIMIT_ERROR);
        if (pagination.getOffset() % pagination.getLimit() > 0)
            throw new ServiceException(PAGINATION_ERROR);
        Sort sort = pagination.getSort();
        return sort == null || sort.isUnsorted()
                ? PageRequest.of(pagination.getOffset() / pagination.getLimit(), pagination.getLimit())
                : PageRequest.of(pagination.getOffset() / pagination.getLimit(), pagination.getLimit(), sort);
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

    //todo drop me when fix twin search for search v3
    public static <T> PaginationResult<T> convertInPaginationResult(List<T> list, SimplePagination pagination) {
        PaginationResult<T> result = new PaginationResult<>();
        result
                .setList(list)
                .setTotal(pagination.getTotalElements())
                .setOffset(pagination.getOffset())
                .setLimit(pagination.getLimit());
        return result;
    }

    //todo drop me when fix twin search for search v3
    public static void validPagination(SimplePagination pagination) throws ServiceException {
        if (pagination.getLimit() < 1)
            throw new ServiceException(PAGINATION_LIMIT_ERROR);
        if (pagination.getOffset() % pagination.getLimit() > 0)
            throw new ServiceException(PAGINATION_ERROR);
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
