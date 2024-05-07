package org.cambium.common.util;

import org.cambium.common.exception.ServiceException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

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

    public static Pageable paginationOffset(int offset, int limit, Sort sort) throws ServiceException {
        if (offset % limit > 0) throw new ServiceException(PAGINATION_ERROR);
        return PageRequest.of(offset / limit, limit, sort);
    }

    public static Pageable paginationOffsetUnsorted(int offset, int limit) throws ServiceException {
        if (offset % limit > 0) throw new ServiceException(PAGINATION_ERROR);
        return PageRequest.of(offset / limit, limit);
    }

}
