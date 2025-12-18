package org.twins.core.mappers.rest.twinflow;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModeBinding;
import org.twins.core.controller.rest.annotation.MapperModePointerBinding;
import org.twins.core.dao.twinflow.TwinflowTransitionTriggerEntity;
import org.twins.core.dto.rest.transition.TransitionTriggerDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.featurer.FeaturerRestDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.FeaturerMode;
import org.twins.core.mappers.rest.mappercontext.modes.TransitionMode;
import org.twins.core.mappers.rest.mappercontext.modes.TransitionTriggerMode;
import org.twins.core.service.twinflow.TwinflowTransitionTriggerService;

import java.util.Collection;


@Component
@RequiredArgsConstructor
@MapperModeBinding(modes = TransitionTriggerMode.class)
public class TransitionTriggerRestDTOMapper extends RestSimpleDTOMapper<TwinflowTransitionTriggerEntity, TransitionTriggerDTOv1> {

    @MapperModePointerBinding(modes = FeaturerMode.TransitionTrigger2FeaturerMode.class)
    private final FeaturerRestDTOMapper featurerRestDTOMapper;

    @MapperModePointerBinding(modes = TransitionMode.TransitionTrigger2TransitionMode.class)
    private final TransitionBaseV1RestDTOMapper transitionRestDTOMapper;
    private final TwinflowTransitionTriggerService twinflowTransitionTriggerService;

    @Override
    public void map(TwinflowTransitionTriggerEntity src, TransitionTriggerDTOv1 dst, MapperContext mapperContext) throws Exception {
        switch (mapperContext.getModeOrUse(TransitionTriggerMode.DETAILED)) {
            case DETAILED -> dst
                    .setId(src.getId())
                    .setOrder(src.getOrder())
                    .setTwinflowTransitionId(src.getTwinflowTransitionId())
                    .setTransitionTriggerFeaturerId(src.getTransitionTriggerFeaturerId())
                    .setTransitionTriggerParams(src.getTransitionTriggerParams())
                    .setActive(src.getIsActive());
            case SHORT -> dst
                    .setId(src.getId())
                    .setTwinflowTransitionId(src.getTwinflowTransitionId())
                    .setTransitionTriggerFeaturerId(src.getTransitionTriggerFeaturerId());
        }
        if (mapperContext.hasModeButNot(FeaturerMode.TransitionTrigger2FeaturerMode.HIDE)) {
            dst.setTransitionTriggerFeaturerId(src.getTransitionTriggerFeaturerId());
            twinflowTransitionTriggerService.loadTrigger(src);
            featurerRestDTOMapper.postpone(src.getTransitionTriggerFeaturer(), mapperContext.forkOnPoint(mapperContext.getModeOrUse(FeaturerMode.TransitionTrigger2FeaturerMode.SHORT)));
        }
        if (mapperContext.hasModeButNot(TransitionMode.TransitionTrigger2TransitionMode.HIDE)) {
            dst.setTwinflowTransitionId(src.getTwinflowTransitionId());
            transitionRestDTOMapper.postpone(src.getTwinflowTransition(), mapperContext.forkOnPoint(mapperContext.getModeOrUse(TransitionMode.TransitionTrigger2TransitionMode.SHORT)));
        }
    }

    @Override
    public void beforeCollectionConversion(Collection<TwinflowTransitionTriggerEntity> srcCollection, MapperContext mapperContext) throws Exception {
        super.beforeCollectionConversion(srcCollection, mapperContext);
        if (mapperContext.hasModeButNot(FeaturerMode.TransitionTrigger2FeaturerMode.HIDE)) {
            twinflowTransitionTriggerService.loadTriggers(srcCollection);
        }
    }
}
