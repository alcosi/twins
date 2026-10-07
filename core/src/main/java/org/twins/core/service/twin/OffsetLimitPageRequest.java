package org.twins.core.service.twin;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

/**
 * Pageable whose offset is not required to be a multiple of the page size.
 * Spring Data applies {@link #getOffset()} via {@code Query.setFirstResult}, so a concatenation
 * window such as offset 6 / limit 10 is a valid query. {@link org.springframework.data.domain.PageRequest}
 * cannot express that, because its offset is always {@code pageNumber * pageSize}.
 */
final class OffsetLimitPageRequest implements Pageable {
    private final int offset;
    private final int limit;
    private final Sort sort;

    OffsetLimitPageRequest(int offset, int limit, Sort sort) {
        this.offset = offset;
        this.limit = limit;
        this.sort = sort == null ? Sort.unsorted() : sort;
    }

    @Override
    public int getPageNumber() {
        return limit == 0 ? 0 : offset / limit;
    }

    @Override
    public int getPageSize() {
        return limit;
    }

    @Override
    public long getOffset() {
        return offset;
    }

    @Override
    public Sort getSort() {
        return sort;
    }

    @Override
    public Pageable next() {
        return new OffsetLimitPageRequest(offset + limit, limit, sort);
    }

    @Override
    public Pageable previousOrFirst() {
        return hasPrevious() ? new OffsetLimitPageRequest(Math.max(0, offset - limit), limit, sort) : this;
    }

    @Override
    public Pageable first() {
        return new OffsetLimitPageRequest(0, limit, sort);
    }

    @Override
    public Pageable withPage(int pageNumber) {
        return new OffsetLimitPageRequest(pageNumber * limit, limit, sort);
    }

    @Override
    public boolean hasPrevious() {
        return offset > 0;
    }
}
