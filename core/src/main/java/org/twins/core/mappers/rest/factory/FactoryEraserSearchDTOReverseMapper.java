package org.twins.core.mappers.rest.factory;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.domain.search.FactoryEraserSearch;
import org.twins.core.dto.rest.factory.FactoryEraserSearchRqDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;


@Component
@RequiredArgsConstructor
public class FactoryEraserSearchDTOReverseMapper extends RestSimpleDTOMapper<FactoryEraserSearchRqDTOv1, FactoryEraserSearch> {

    @Override
    public void map(FactoryEraserSearchRqDTOv1 src, FactoryEraserSearch dst, MapperContext mapperContext) {
        dst
                .setIdList(src.getIdList())
                .setIdExcludeList(src.getIdExcludeList())
                .setFactoryIdList(src.getFactoryIdList())
                .setFactoryIdExcludeList(src.getFactoryIdExcludeList())
                .setInputTwinClassIdList(src.getInputTwinClassIdList())
                .setInputTwinClassIdExcludeList(src.getInputTwinClassIdExcludeList())
                .setFactoryConditionSetIdList(src.getFactoryConditionSetIdList())
                .setFactoryConditionSetIdExcludeList(src.getFactoryConditionSetIdExcludeList())
                .setConditionInvert(src.getConditionInvert())
                .setDescriptionLikeList(src.getDescriptionLikeList())
                .setDescriptionNotLikeList(src.getDescriptionNotLikeList())
                .setEraseActionLikeList(src.getEraseActionLikeList())
                .setEraseActionNotLikeList(src.getEraseActionNotLikeList())
                .setActive(src.getActive());
    }
}
