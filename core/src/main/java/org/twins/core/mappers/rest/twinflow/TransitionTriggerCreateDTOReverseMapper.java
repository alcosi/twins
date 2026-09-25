package org.twins.core.mappers.rest.twinflow;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twinflow.TwinflowTransitionTriggerEntity;
import org.twins.core.dto.rest.transition.TransitionTriggerCreateDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
@RequiredArgsConstructor
public class TransitionTriggerCreateDTOReverseMapper extends RestSimpleDTOMapper<TransitionTriggerCreateDTOv1, TwinflowTransitionTriggerEntity> {

    @Override
    public void map(TransitionTriggerCreateDTOv1 src, TwinflowTransitionTriggerEntity dst, MapperContext mapperContext) throws Exception {
        dst
                .setOrder(src.getOrder())
                .setActive(src.getActive())
                .setAsync(src.getAsync())
                .setTwinflowTransitionId(src.getTwinflowTransitionId())
                .setTwinTriggerId(src.getTwinTriggerId());
    }
}
