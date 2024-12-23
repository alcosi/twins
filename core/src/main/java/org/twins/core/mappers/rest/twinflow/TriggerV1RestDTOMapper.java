package org.twins.core.mappers.rest.twinflow;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModePointerBinding;
import org.twins.core.dao.twinflow.TwinflowTransitionTriggerEntity;
import org.twins.core.dto.rest.twinflow.TriggerDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.featurer.FeaturerRestDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.FactoryMode;
import org.twins.core.mappers.rest.mappercontext.modes.FeaturerMode;
import org.twins.core.mappers.rest.mappercontext.modes.TransitionMode;

@Component
@RequiredArgsConstructor
public class TriggerV1RestDTOMapper extends RestSimpleDTOMapper<TwinflowTransitionTriggerEntity, TriggerDTOv1> {

    private final TriggerBaseV1RestDTOMapper triggerBaseV1RestDTOMapper;

    @MapperModePointerBinding(modes = FeaturerMode.Trigger2FeaturerMode.class)
    private final FeaturerRestDTOMapper featurerRestDTOMapper;

    @Override
    public void map(TwinflowTransitionTriggerEntity src, TriggerDTOv1 dst, MapperContext mapperContext) throws Exception {
        triggerBaseV1RestDTOMapper.map(src, dst, mapperContext);
        dst.setId(src.getId());
        if (mapperContext.hasModeButNot(FeaturerMode.Trigger2FeaturerMode.HIDE))
            dst
                    .setTriggerFeaturer(featurerRestDTOMapper.convertOrPostpone(src.getTransitionTriggerFeaturer(), mapperContext.forkOnPoint(FeaturerMode.Trigger2FeaturerMode.SHORT)))
                    .setTriggerFeaturerId(src.getTransitionTriggerFeaturerId());
    }

    @Override
    public String getObjectCacheId(TwinflowTransitionTriggerEntity src) {
        return src.getId().toString();
    }

    @Override
    public boolean hideMode(MapperContext mapperContext) {
        return mapperContext.hasModeOrEmpty(TransitionMode.HIDE);
    }

}
