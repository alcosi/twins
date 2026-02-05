package org.twins.core.mappers.rest.twinflow;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModePointerBinding;
import org.twins.core.dao.twinflow.TwinflowTransitionTriggerEntity;
import org.twins.core.dto.rest.twinflow.TriggerDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.TransitionMode;
import org.twins.core.mappers.rest.mappercontext.modes.TwinTriggerMode;
import org.twins.core.mappers.rest.trigger.TwinTriggerRestDTOMapper;
import org.twins.core.service.twinflow.TwinflowTransitionTriggerService;

import java.util.Collection;

@Component
@RequiredArgsConstructor
public class TriggerV1RestDTOMapper extends RestSimpleDTOMapper<TwinflowTransitionTriggerEntity, TriggerDTOv1> {

    private final TriggerBaseV1RestDTOMapper triggerBaseV1RestDTOMapper;

    @MapperModePointerBinding(modes = TwinTriggerMode.TransitionTrigger2TwinTriggerMode.class)
    private final TwinTriggerRestDTOMapper twinTriggerRestDTOMapper;
    private final TwinflowTransitionTriggerService twinflowTransitionTriggerService;

    @Override
    public void map(TwinflowTransitionTriggerEntity src, TriggerDTOv1 dst, MapperContext mapperContext) throws Exception {
        triggerBaseV1RestDTOMapper.map(src, dst, mapperContext);
        dst.setId(src.getId());
        if (mapperContext.hasModeButNot(TwinTriggerMode.TransitionTrigger2TwinTriggerMode.HIDE)) {
            twinflowTransitionTriggerService.loadTrigger(src);
            dst
                    .setTwinTrigger(twinTriggerRestDTOMapper.convertOrPostpone(src.getTwinTrigger(), mapperContext.forkOnPoint(TwinTriggerMode.TransitionTrigger2TwinTriggerMode.SHORT)));
        }
    }

    @Override
    public void beforeCollectionConversion(Collection<TwinflowTransitionTriggerEntity> srcCollection, MapperContext mapperContext) throws Exception {
        super.beforeCollectionConversion(srcCollection, mapperContext);
        if (mapperContext.hasModeButNot(TwinTriggerMode.TransitionTrigger2TwinTriggerMode.HIDE)) {
            twinflowTransitionTriggerService.loadTriggers(srcCollection);
        }
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
