package org.twins.core.mappers.rest.pagination;

import org.springframework.stereotype.Component;
import org.twins.core.dto.rest.pagination.PaginationDTOv1;
import org.twins.core.mappers.rest.MapperContext;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.service.pagination.PageableResult;

@Component
public class PaginationMapper extends RestSimpleDTOMapper<PageableResult,PaginationDTOv1> {
    @Override
    public void map(PageableResult src, PaginationDTOv1 dst, MapperContext mapperContext) throws Exception {
        dst
                .setPage(src.getPage())
                .setCount(src.getCount())
                .setTotal(src.getTotal());
    }
}
