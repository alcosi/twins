package org.twins.core.mappers.rest.domain;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.domain.search.DomainBusinessAccountUserSearch;
import org.twins.core.dto.rest.domain.DomainBusinessAccountUserSearchRqDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOConverter;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
@RequiredArgsConstructor
public class DomainBusinessAccountUserSearchRqDTOReverseMapper extends RestSimpleDTOConverter<DomainBusinessAccountUserSearchRqDTOv1, DomainBusinessAccountUserSearch> {
    private final DomainBusinessAccountUserSearchDTOReverseMapper domainBusinessAccountUserSearchDTOReverseMapper;

    @Override
    public DomainBusinessAccountUserSearch convert(DomainBusinessAccountUserSearchRqDTOv1 src, MapperContext mapperContext) throws Exception {
        var dst = domainBusinessAccountUserSearchDTOReverseMapper.convert(src.getSearch(), mapperContext);
        dst
                .setSortField(src.getSortField())
                .setSortDirection(src.getSortDirection());
        return dst;
    }
}
