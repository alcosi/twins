package org.twins.core.mappers.rest.twinstatus;

import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinStatusTransitionTriggerEntity;
import org.twins.core.dto.rest.twinstatus.TwinStatusTransitionTriggerSaveDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
public class TwinStatusTransitionTriggerSaveDTOReverseMapper extends RestSimpleDTOMapper<TwinStatusTransitionTriggerSaveDTOv1, TwinStatusTransitionTriggerEntity> {

    @Override
    public void map(TwinStatusTransitionTriggerSaveDTOv1 src, TwinStatusTransitionTriggerEntity dst, MapperContext mapperContext) throws Exception {
        dst
                .setTwinStatusId(src.getTwinStatusId())
                .setType(src.getType())
                .setOrder(src.getOrder())
                .setTwinTriggerId(src.getTwinTriggerId())
                .setAsync(src.getAsync())
                .setActive(src.getActive() != null ? src.getActive() : true);
    }
}
