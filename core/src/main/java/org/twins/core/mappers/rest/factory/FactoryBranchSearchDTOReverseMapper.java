package org.twins.core.mappers.rest.factory;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.domain.search.FactoryBranchSearch;
import org.twins.core.dto.rest.factory.FactoryBranchSearchRqDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
@RequiredArgsConstructor
public class FactoryBranchSearchDTOReverseMapper extends RestSimpleDTOMapper<FactoryBranchSearchRqDTOv1, FactoryBranchSearch> {

    @Override
    public void map(FactoryBranchSearchRqDTOv1 src, FactoryBranchSearch dst, MapperContext mapperContext) {
        dst
                .setIdList(src.getIdList())
                .setIdExcludeList(src.getIdExcludeList())
                .setFactoryIdList(src.getFactoryIdList())
                .setFactoryIdExcludeList(src.getFactoryIdExcludeList())
                .setFactoryConditionSetIdList(src.getFactoryConditionSetIdList())
                .setFactoryConditionSetIdExcludeList(src.getFactoryConditionSetIdExcludeList())
                .setNextFactoryIdList(src.getNextFactoryIdList())
                .setNextFactoryIdExcludeList(src.getNextFactoryIdExcludeList())
                .setDescriptionLikeList(src.getDescriptionLikeList())
                .setDescriptionNotLikeList(src.getDescriptionNotLikeList())
                .setActive(src.getActive());
    }
}
