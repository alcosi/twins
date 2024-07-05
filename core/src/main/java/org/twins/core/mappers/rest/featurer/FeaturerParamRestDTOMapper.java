package org.twins.core.mappers.rest.featurer;

import lombok.RequiredArgsConstructor;
import org.cambium.featurer.dao.FeaturerParamEntity;
import org.springframework.stereotype.Component;
import org.twins.core.dto.rest.featurer.FeaturerParamDTOv1;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;

@Component
@RequiredArgsConstructor
public class FeaturerParamRestDTOMapper extends RestSimpleDTOMapper<FeaturerParamEntity, FeaturerParamDTOv1> {
    @Override
    public void map(FeaturerParamEntity src, FeaturerParamDTOv1 dst, MapperContext mapperContext) throws Exception {
        dst
                .setKey(src.getKey())
                .setName(src.getName())
                .setDescription(src.getDescription())
                .setType(src.getFeaturerParamTypeId());
    }
}
