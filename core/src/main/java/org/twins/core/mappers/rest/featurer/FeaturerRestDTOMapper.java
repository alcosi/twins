package org.twins.core.mappers.rest.featurer;

import lombok.RequiredArgsConstructor;
import org.cambium.featurer.FeaturerService;
import org.cambium.featurer.dao.FeaturerEntity;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModeBinding;
import org.twins.core.dto.rest.featurer.FeaturerDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.FeaturerMode;
import org.twins.core.mappers.rest.mappercontext.modes.FeaturerParamMode;
import org.twins.core.mappers.rest.mappercontext.modes.FeaturerTypeMode;

import java.util.Collection;

@Component
@RequiredArgsConstructor
@MapperModeBinding(modes = {
        FeaturerMode.class,
        FeaturerParamMode.class,
        FeaturerTypeMode.class
})
public class FeaturerRestDTOMapper extends RestSimpleDTOMapper<FeaturerEntity, FeaturerDTOv1> {

    private final FeaturerParamRestDTOMapper featurerParamRestDTOMapper;
    private final FeaturerTypeRestDTOMapper featurerTypeRestDTOMapper;
    private final FeaturerService featurerService;

    @Override
    public void map(FeaturerEntity src, FeaturerDTOv1 dst, MapperContext mapperContext) throws Exception {
        switch (mapperContext.getModeOrUse(FeaturerMode.DETAILED)) {
            case DETAILED:
                dst
                        .setId(src.getId())
                        .setName(src.getName())
                        .setDescription(src.getDescription())
                        .setFeaturerTypeId(src.getFeaturerTypeId())
                        .setDeprecated(src.isDeprecated());
                if (showFeaturerType(mapperContext)) {
                    featurerService.loadFeaturerTypes(src);
                    dst.setFeaturerType(featurerTypeRestDTOMapper.convert(src.getFeaturerType(), mapperContext));
                }
            case SHORT:
                dst
                        .setId(src.getId())
                        .setName(src.getName());
        }
        if (showFeaturerParams(mapperContext)) {
            featurerService.loadFeaturerParams(src);
            dst
                    .setParams(featurerParamRestDTOMapper.convertCollection(src.getParams().getCollection(), mapperContext));
        }
    }

    private static boolean showFeaturerParams(MapperContext mapperContext) {
        return mapperContext.hasModeButNot(FeaturerParamMode.HIDE);
    }

    private static boolean showFeaturerType(MapperContext mapperContext) {
        return mapperContext.hasModeButNot(FeaturerTypeMode.HIDE);
    }

    @Override
    public void beforeCollectionConversion(Collection<FeaturerEntity> srcCollection, MapperContext mapperContext) throws Exception {
        super.beforeCollectionConversion(srcCollection, mapperContext);
        if (showFeaturerParams(mapperContext))
            featurerService.loadFeaturerParams(srcCollection);
        if (showFeaturerType(mapperContext))
            featurerService.loadFeaturerTypes(srcCollection);
    }

    @Override
    public boolean hideMode(MapperContext mapperContext) {
        return mapperContext.hasModeOrEmpty(FeaturerMode.HIDE);
    }

    public void postpone(Integer featurerId, MapperContext mapperContext) {
        if (featurerId != null) {
            postpone(featurerService.getFeaturerEntity(featurerId), mapperContext);
        }
    }
}
