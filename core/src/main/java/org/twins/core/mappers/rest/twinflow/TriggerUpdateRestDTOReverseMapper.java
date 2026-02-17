package org.twins.core.mappers.rest.twinflow;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twinflow.TwinflowTransitionTriggerEntity;
import org.twins.core.dto.rest.trigger.TwinTriggerUpdateDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;


@Component
@RequiredArgsConstructor
public class TriggerUpdateRestDTOReverseMapper extends RestSimpleDTOMapper<TwinTriggerUpdateDTOv1, TwinflowTransitionTriggerEntity> {

    final TriggerBaseV1RestDTOReverseMapper triggerBaseV1RestDTOReverseMapper;

    @Override
    public void map(TwinTriggerUpdateDTOv1 src, TwinflowTransitionTriggerEntity dst, MapperContext mapperContext) throws Exception {
        triggerBaseV1RestDTOReverseMapper.map(src, dst, mapperContext);
        dst.setId(src.getId());
    }
}
