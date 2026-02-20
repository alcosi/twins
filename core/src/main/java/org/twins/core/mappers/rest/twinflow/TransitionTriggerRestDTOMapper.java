package org.twins.core.mappers.rest.twinflow;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModeBinding;
import org.twins.core.controller.rest.annotation.MapperModePointerBinding;
import org.twins.core.dao.twinflow.TwinflowTransitionTriggerEntity;
import org.twins.core.dto.rest.transition.TransitionTriggerDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.TransitionMode;
import org.twins.core.mappers.rest.mappercontext.modes.TransitionTriggerMode;
import org.twins.core.mappers.rest.trigger.TwinTriggerRestDTOMapper;
import org.twins.core.mappers.rest.mappercontext.modes.TwinTriggerMode;
import org.twins.core.service.twinflow.TwinflowTransitionTriggerService;

import java.util.Collection;


@Component
@RequiredArgsConstructor
@MapperModeBinding(modes = TransitionTriggerMode.class)
public class TransitionTriggerRestDTOMapper extends RestSimpleDTOMapper<TwinflowTransitionTriggerEntity, TransitionTriggerDTOv1> {

    @MapperModePointerBinding(modes = TwinTriggerMode.TransitionTrigger2TwinTriggerMode.class)
    private final TwinTriggerRestDTOMapper twinTriggerRestDTOMapper;

    @MapperModePointerBinding(modes = TransitionTriggerMode.TransitionTrigger2TransitionTriggerMode.class)
    private final TransitionBaseV1RestDTOMapper transitionRestDTOMapper;
    private final TwinflowTransitionTriggerService twinflowTransitionTriggerService;

    @Override
    public void map(TwinflowTransitionTriggerEntity src, TransitionTriggerDTOv1 dst, MapperContext mapperContext) throws Exception {
        switch (mapperContext.getModeOrUse(TransitionTriggerMode.DETAILED)) {
            case DETAILED -> dst
                    .setId(src.getId())
                    .setOrder(src.getOrder())
                    .setTwinflowTransitionId(src.getTwinflowTransitionId())
                    .setTwinTriggerId(src.getTwinTriggerId())
                    .setAsync(src.getAsync())
                    .setActive(src.getIsActive());
            case SHORT -> dst
                    .setId(src.getId())
                    .setTwinflowTransitionId(src.getTwinflowTransitionId())
                    .setTwinTriggerId(src.getTwinTriggerId());
        }
        if (mapperContext.hasModeButNot(TwinTriggerMode.TransitionTrigger2TwinTriggerMode.HIDE)) {
            dst.setTwinTriggerId(src.getTwinTriggerId());
            twinflowTransitionTriggerService.loadTrigger(src);
            twinTriggerRestDTOMapper.postpone(src.getTwinTrigger(), mapperContext.forkOnPoint(mapperContext.getModeOrUse(TwinTriggerMode.TransitionTrigger2TwinTriggerMode.SHORT)));
        }
        if (mapperContext.hasModeButNot(TransitionTriggerMode.TransitionTrigger2TransitionTriggerMode.HIDE)) {
            dst.setTwinflowTransitionId(src.getTwinflowTransitionId());
            transitionRestDTOMapper.postpone(src.getTwinflowTransition(), mapperContext.forkOnPoint(mapperContext.getModeOrUse(TransitionTriggerMode.TransitionTrigger2TransitionTriggerMode.SHORT)));
        }
    }

    @Override
    public void beforeCollectionConversion(Collection<TwinflowTransitionTriggerEntity> srcCollection, MapperContext mapperContext) throws Exception {
        super.beforeCollectionConversion(srcCollection, mapperContext);
        if (mapperContext.hasModeButNot(TwinTriggerMode.TransitionTrigger2TwinTriggerMode.HIDE)) {
            twinflowTransitionTriggerService.loadTriggers(srcCollection);
        }
    }
}
