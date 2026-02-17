package org.twins.core.mappers.rest.twinflow;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twinflow.TwinflowTransitionTriggerEntity;
import org.twins.core.dto.rest.trigger.TwinTriggerSaveDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
@RequiredArgsConstructor
public class TriggerBaseV1RestDTOReverseMapper extends RestSimpleDTOMapper<TwinTriggerSaveDTOv1, TwinflowTransitionTriggerEntity> {

    @Override
    public void map(TwinTriggerSaveDTOv1 src, TwinflowTransitionTriggerEntity dst, MapperContext mapperContext) throws Exception {
        dst
                .setOrder(src.getOrder())
                .setTwinTriggerId(src.getTwinTriggerId())
                .setAsync(src.getAsync())
                .setIsActive(src.getActive());
    }
}
