package org.twins.core.mappers.rest.featurer;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.domain.search.FeaturerSearch;
import org.twins.core.dto.rest.featurer.FeaturerSearchDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
@RequiredArgsConstructor
public class FeaturerSearchDTOReverseMapper extends RestSimpleDTOMapper<FeaturerSearchDTOv1, FeaturerSearch> {

    @Override
    public void map(FeaturerSearchDTOv1 src, FeaturerSearch dst, MapperContext mapperContext) throws Exception {
        dst
                .setIdList(src.getIdList())
                .setIdExcludeList(src.getIdExcludeList())
                .setTypeIdList(src.getTypeIdList())
                .setTypeIdExcludeList(src.getTypeIdExcludeList())
                .setNameLikeList(src.getNameLikeList())
                .setNameNotLikeList(src.getNameNotLikeList())
                .setDescriptionLikeList(src.getDescriptionLikeList())
                .setDescriptionNotLikeList(src.getDescriptionNotLikeList())
                .setDeprecated(src.getDeprecated());
    }
}
