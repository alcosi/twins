package org.twins.core.mappers.rest.twinflow;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twinflow.TwinflowTransitionTriggerEntity;
import org.twins.core.dto.rest.twinflow.TriggerCreateDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;


@Component
@RequiredArgsConstructor
public class TriggerCreateRestDTOReverseMapper extends RestSimpleDTOMapper<TriggerCreateDTOv1, TwinflowTransitionTriggerEntity> {

    final TriggerBaseV1RestDTOReverseMapper triggerBaseV1RestDTOReverseMapper;

    @Override
    public void map(TriggerCreateDTOv1 src, TwinflowTransitionTriggerEntity dst, MapperContext mapperContext) throws Exception {
        triggerBaseV1RestDTOReverseMapper.map(src, dst, mapperContext);
    }
}
