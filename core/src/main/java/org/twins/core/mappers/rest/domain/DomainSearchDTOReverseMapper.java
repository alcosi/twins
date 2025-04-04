package org.twins.core.mappers.rest.domain;

import org.springframework.stereotype.Component;
import org.twins.core.domain.search.DomainSearch;
import org.twins.core.dto.rest.domain.DomainSearchRqDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
public class DomainSearchDTOReverseMapper extends RestSimpleDTOMapper<DomainSearchRqDTOv1, DomainSearch> {
    @Override
    public void map(DomainSearchRqDTOv1 src, DomainSearch dst, MapperContext mapperContext) throws Exception {
        dst
                .setKeyLikeList(src.getKeyLikeList())
                .setKeyNotLikeList(src.getKeyNotLikeList());
    }
}
