package org.twins.core.mappers.rest.factory;

import org.springframework.stereotype.Component;
import org.twins.core.domain.search.FactorySearch;
import org.twins.core.dto.rest.factory.FactorySearchRqDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
public class FactorySearchDTOReverseMapper extends RestSimpleDTOMapper<FactorySearchRqDTOv1, FactorySearch> {
    @Override
    public void map(FactorySearchRqDTOv1 src, FactorySearch dst, MapperContext mapperContext) {
        dst
                .setIdList(src.getIdList())
                .setIdExcludeList(src.getIdExcludeList())
                .setKeyLikeList(src.getKeyLikeList())
                .setKeyNotLikeList(src.getKeyNotLikeList())
                .setNameLikeList(src.getNameLikeList())
                .setNameNotLikeList(src.getNameNotLikeList())
                .setDescriptionLikeList(src.getDescriptionLikeList())
                .setDescriptionNotLikeList(src.getDescriptionNotLikeList());
    }
}
