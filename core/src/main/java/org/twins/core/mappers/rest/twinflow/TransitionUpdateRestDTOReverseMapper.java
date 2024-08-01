package org.twins.core.mappers.rest.twinflow;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twinflow.TwinflowTransitionEntity;
import org.twins.core.dto.rest.twinflow.TransitionUpdateRqDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;


@Component
@RequiredArgsConstructor
public class TransitionUpdateRestDTOReverseMapper extends RestSimpleDTOMapper<TransitionUpdateRqDTOv1, TwinflowTransitionEntity> {

    private final TransitionSaveRestDTOReverseMapper transitionSaveRestDTOReverseMapper;


    @Override
    public void map(TransitionUpdateRqDTOv1 src, TwinflowTransitionEntity dst, MapperContext mapperContext) throws Exception {
        transitionSaveRestDTOReverseMapper.map(src, dst,  mapperContext);
    }
}
