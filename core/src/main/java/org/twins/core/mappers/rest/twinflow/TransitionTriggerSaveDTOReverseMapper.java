package org.twins.core.mappers.rest.twinflow;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twinflow.TwinflowTransitionTriggerEntity;
import org.twins.core.dto.rest.transition.TransitionTriggerSaveDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
@RequiredArgsConstructor
public class TransitionTriggerSaveDTOReverseMapper extends RestSimpleDTOMapper<TransitionTriggerSaveDTOv1, TwinflowTransitionTriggerEntity> {

    @Override
    public void map(TransitionTriggerSaveDTOv1 src, TwinflowTransitionTriggerEntity dst, MapperContext mapperContext) throws Exception {
        dst
                .setId(src.getId())
                .setOrder(src.getOrder())
                .setIsActive(src.getActive())
                .setAsync(src.getAsync())
                .setTwinflowTransitionId(src.getTwinflowTransitionId())
                .setTwinTriggerId(src.getTwinTriggerId());
    }
}
