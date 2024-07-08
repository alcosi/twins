package org.twins.core.mappers.rest.pagination;

import org.cambium.common.pagination.PaginationResult;
import org.springframework.stereotype.Component;
import org.twins.core.dto.rest.pagination.PaginationDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
public class PaginationMapper extends RestSimpleDTOMapper<PaginationResult<?>, PaginationDTOv1> {
    @Override
    public void map(PaginationResult<?> src, PaginationDTOv1 dst, MapperContext mapperContext) throws Exception {
        dst
                .setOffset(src.getOffset())
                .setLimit(src.getLimit())
                .setTotal(src.getTotal());
    }
}
