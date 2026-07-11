package org.twins.core.mappers.rest.factory;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.domain.search.FactoryConditionSetSearch;
import org.twins.core.dto.rest.factory.FactoryConditionSetSearchDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
@RequiredArgsConstructor
public class FactoryConditionSetSearchDTOReverseMapper extends RestSimpleDTOMapper<FactoryConditionSetSearchDTOv1, FactoryConditionSetSearch> {

    @Override
    public void map(FactoryConditionSetSearchDTOv1 src, FactoryConditionSetSearch dst, MapperContext mapperContext) {
        dst
                .setIdList(src.getIdList())
                .setIdExcludeList(src.getIdExcludeList())
                .setTwinFactoryIdList(src.getTwinFactoryIdList())
                .setTwinFactoryIdExcludeList(src.getTwinFactoryIdExcludeList())
                .setNameLikeList(src.getNameLikeList())
                .setNameNotLikeList(src.getNameNotLikeList())
                .setDescriptionLikeList(src.getDescriptionLikeList())
                .setDescriptionNotLikeList(src.getDescriptionNotLikeList())
                .setCachable(src.getCachable());
    }
}
