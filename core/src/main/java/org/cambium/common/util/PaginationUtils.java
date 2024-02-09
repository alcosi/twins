package org.cambium.common.util;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public class PaginationUtils {

    public static Sort sort(boolean asc, String field){
        Sort.Direction direction = Sort.Direction.DESC;
        if(asc) direction = Sort.Direction.ASC;
        return Sort.by(direction, field);
    }

    public static Pageable pagination(int page, int size) {
        return PageRequest.of(page, size);
    }

    public static Pageable pagination(int page, int size, Sort sort) {
        return PageRequest.of(page, size, sort);
    }


}
