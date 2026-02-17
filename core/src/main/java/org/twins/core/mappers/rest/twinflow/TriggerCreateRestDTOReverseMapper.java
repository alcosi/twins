package org.twins.core.mappers.rest.twinflow;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twinflow.TwinflowTransitionTriggerEntity;
import org.twins.core.dto.rest.trigger.TwinTriggerCreateDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;


@Component
@RequiredArgsConstructor
public class TriggerCreateRestDTOReverseMapper extends RestSimpleDTOMapper<TwinTriggerCreateDTOv1, TwinflowTransitionTriggerEntity> {

    final TriggerBaseV1RestDTOReverseMapper triggerBaseV1RestDTOReverseMapper;

    @Override
    public void map(TwinTriggerCreateDTOv1 src, TwinflowTransitionTriggerEntity dst, MapperContext mapperContext) throws Exception {
        triggerBaseV1RestDTOReverseMapper.map(src, dst, mapperContext);
    }
}
