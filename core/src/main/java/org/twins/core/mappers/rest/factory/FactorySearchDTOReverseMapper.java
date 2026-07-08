package org.twins.core.mappers.rest.factory;

import org.springframework.stereotype.Component;
import org.twins.core.domain.search.FactorySearch;
import org.twins.core.dto.rest.factory.FactorySearchDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
public class FactorySearchDTOReverseMapper extends RestSimpleDTOMapper<FactorySearchDTOv1, FactorySearch> {
    @Override
    public void map(FactorySearchDTOv1 src, FactorySearch dst, MapperContext mapperContext) {
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
