package org.twins.core.mappers.rest.featurer;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.domain.search.FeaturerSearch;
import org.twins.core.dto.rest.featurer.FeaturerSearchRqDTOv1;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;

@Component
@RequiredArgsConstructor
public class FeaturerDTOReversMapper extends RestSimpleDTOMapper<FeaturerSearchRqDTOv1, FeaturerSearch> {

    @Override
    public void map(FeaturerSearchRqDTOv1 src, FeaturerSearch dst, MapperContext mapperContext) throws Exception {
        dst
                .setIdList(src.getIdList())
                .setTypeIdList(src.getTypeIdList())
                .setNameLikeList(src.getNameLikeList());
    }
}
