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

    private final TransitionTriggerSaveDTOReverseMapper transitionTriggerSaveDTOReverseMapper;

    @Override
    public void map(TransitionTriggerCreateDTOv1 src, TwinflowTransitionTriggerEntity dst, MapperContext mapperContext) throws Exception {
        transitionTriggerSaveDTOReverseMapper.map(src, dst, mapperContext);
    }
}
