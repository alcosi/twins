package org.twins.core.mappers.rest.featurer;

import lombok.RequiredArgsConstructor;
import org.cambium.featurer.FeaturerService;
import org.cambium.featurer.dao.FeaturerEntity;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModeBinding;
import org.twins.core.dto.rest.featurer.FeaturerDTOv1;
import org.twins.core.mappers.rest.MapperContext;
import org.twins.core.mappers.rest.MapperMode;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;

import java.util.Collection;

@Component
@RequiredArgsConstructor
@MapperModeBinding(modes = {
        MapperMode.FeaturerMode.class,
        MapperMode.FeaturerParamMode.class
})
public class FeaturerRestDTOMapper extends RestSimpleDTOMapper<FeaturerEntity, FeaturerDTOv1> {

    private final FeaturerParamRestDTOMapper featurerParamRestDTOMapper;
    private final FeaturerService featurerService;

    @Override
    public void map(FeaturerEntity src, FeaturerDTOv1 dst, MapperContext mapperContext) throws Exception {
        switch (mapperContext.getModeOrUse(MapperMode.FeaturerMode.DETAILED)) {
            case DETAILED:
                dst
                        .setId(src.getId())
                        .setName(src.getName())
                        .setDescription(src.getDescription())
                        .setFeaturerTypeId(src.getFeaturerTypeId())
                        .setDeprecated(src.isDeprecated());
            case SHORT:
                dst
                        .setId(src.getId())
                        .setName(src.getName());
        }
        if (showFeaturerParams(mapperContext))
            dst
                    .setParams(featurerParamRestDTOMapper.convertCollection(src.getParams(), mapperContext));
    }

    private static boolean showFeaturerParams(MapperContext mapperContext) {
        return mapperContext.hasModeButNot(MapperMode.FeaturerParamMode.HIDE);
    }

    @Override
    public void beforeCollectionConversion(Collection<FeaturerEntity> srcCollection, MapperContext mapperContext) throws Exception {
        super.beforeCollectionConversion(srcCollection, mapperContext);
        if (showFeaturerParams(mapperContext))
            featurerService.loadFeaturerParams(srcCollection);
    }

    @Override
    public boolean hideMode(MapperContext mapperContext) {
        return mapperContext.hasModeOrEmpty(MapperMode.FeaturerMode.HIDE);
    }

}
