package org.twins.core.mappers.rest.domain;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.domain.search.DomainBusinessAccountSearch;
import org.twins.core.dto.rest.domain.DomainBusinessAccountSearchRqDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOConverter;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
@RequiredArgsConstructor
public class DomainBusinessAccountSearchRqDTOReverseMapper extends RestSimpleDTOConverter<DomainBusinessAccountSearchRqDTOv1, DomainBusinessAccountSearch> {
    private final DomainBusinessAccountSearchRestDTOReverseMapper domainBusinessAccountSearchRestDTOReverseMapper;

    @Override
    public DomainBusinessAccountSearch convert(DomainBusinessAccountSearchRqDTOv1 src, MapperContext mapperContext) throws Exception {
        var dst = domainBusinessAccountSearchRestDTOReverseMapper.convert(src.getSearch(), mapperContext);
        if (dst == null && src.getSortField() != null)
            dst = new DomainBusinessAccountSearch();
        if (dst != null) {
            dst
                    .setSortField(src.getSortField())
                    .setSortDirection(src.getSortDirection());
        }
        return dst;
    }
}
