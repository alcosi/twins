package org.twins.core.mappers.rest.featurer;

import org.cambium.featurer.dao.FeaturerTypeEntity;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModeBinding;
import org.twins.core.dto.rest.featurer.FeaturerTypeDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.FeaturerTypeMode;

@Component
@MapperModeBinding(modes = FeaturerTypeMode.class)
public class FeaturerTypeRestDTOMapper extends RestSimpleDTOMapper<FeaturerTypeEntity, FeaturerTypeDTOv1> {

    @Override
    public void map(FeaturerTypeEntity src, FeaturerTypeDTOv1 dst, MapperContext mapperContext) {
        dst
                .setId(src.getId())
                .setName(src.getName())
                .setDescription(src.getDescription());
    }
}
