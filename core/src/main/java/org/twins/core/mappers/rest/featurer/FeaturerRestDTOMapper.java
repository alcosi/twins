package org.twins.core.mappers.rest.featurer;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.cambium.featurer.FeaturerService;
import org.cambium.featurer.dao.FeaturerEntity;
import org.springframework.stereotype.Component;
import org.twins.core.dto.rest.featurer.FeaturerDTOv1;
import org.twins.core.mappers.rest.MapperContext;
import org.twins.core.mappers.rest.MapperMode;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;

import java.util.Collection;

@Component
@RequiredArgsConstructor
public class FeaturerRestDTOMapper extends RestSimpleDTOMapper<FeaturerEntity, FeaturerDTOv1> {
    final FeaturerParamRestDTOMapper featurerParamRestDTOMapper;
    final FeaturerService featurerService;

    @Override
    public void map(FeaturerEntity src, FeaturerDTOv1 dst, MapperContext mapperContext) throws Exception {
        switch (mapperContext.getModeOrUse(Mode.DETAILED)) {
            case DETAILED:
                dst
                        .setId(src.getId())
                        .setName(src.getName())
                        .setDescription(src.getDescription())
                        .setFeaturerTypeId(src.getFeaturerTypeId())
                        .setDeprecated(src.isDeprecated());
                if (showFeaturerParams(mapperContext))
                    dst
                        .setParams(featurerParamRestDTOMapper.convertList(src.getParams(), mapperContext));
            case SHORT:
                dst
                        .setId(src.getId())
                        .setName(src.getName());
        }
    }

    private static boolean showFeaturerParams(MapperContext mapperContext) {
        return !mapperContext.hasModeOrEmpty(ShowFeaturerParamMode.HIDE);
    }

    @Override
    public void beforeListConversion(Collection<FeaturerEntity> srcCollection, MapperContext mapperContext) throws Exception {
        super.beforeListConversion(srcCollection, mapperContext);
        if (showFeaturerParams(mapperContext)) {
            featurerService.loadFeaturerParams(srcCollection);
        }
    }

    @Override
    public boolean hideMode(MapperContext mapperContext) {
        return mapperContext.hasModeOrEmpty(Mode.HIDE);
    }

    @AllArgsConstructor
    public enum Mode implements MapperMode {
        HIDE(0),
        SHORT(1),
        DETAILED(2);

        public static final String _HIDE = "HIDE";
        public static final String _SHORT = "SHORT";
        public static final String _DETAILED = "DETAILED";

        @Getter
        final int priority;
    }

    @AllArgsConstructor
    public enum ShowFeaturerParamMode implements MapperMode {
        HIDE(0),
        SHOW(1);
        public static final String _SHOW = "SHOW";
        public static final String _HIDE = "HIDE";
        @Getter
        final int priority;
    }
}
