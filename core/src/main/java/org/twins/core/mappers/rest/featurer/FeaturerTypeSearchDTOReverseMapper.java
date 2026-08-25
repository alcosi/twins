package org.twins.core.mappers.rest.featurer;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.domain.search.FeaturerTypeSearch;
import org.twins.core.dto.rest.featurer.FeaturerTypeSearchDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
@RequiredArgsConstructor
public class FeaturerTypeSearchDTOReverseMapper extends RestSimpleDTOMapper<FeaturerTypeSearchDTOv1, FeaturerTypeSearch> {

    @Override
    public void map(FeaturerTypeSearchDTOv1 src, FeaturerTypeSearch dst, MapperContext mapperContext) throws Exception {
        dst
                .setIdList(src.getIdList())
                .setIdExcludeList(src.getIdExcludeList())
                .setNameLikeList(src.getNameLikeList())
                .setNameNotLikeList(src.getNameNotLikeList())
                .setDescriptionLikeList(src.getDescriptionLikeList())
                .setDescriptionNotLikeList(src.getDescriptionNotLikeList());
    }
}
