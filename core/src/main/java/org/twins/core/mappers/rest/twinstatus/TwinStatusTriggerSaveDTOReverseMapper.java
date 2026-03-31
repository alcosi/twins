package org.twins.core.mappers.rest.twinstatus;

import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinStatusTriggerEntity;
import org.twins.core.dto.rest.twinstatus.TwinStatusTriggerSaveDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
public class TwinStatusTriggerSaveDTOReverseMapper extends RestSimpleDTOMapper<TwinStatusTriggerSaveDTOv1, TwinStatusTriggerEntity> {

    @Override
    public void map(TwinStatusTriggerSaveDTOv1 src, TwinStatusTriggerEntity dst, MapperContext mapperContext) throws Exception {
        dst
                .setTwinStatusId(src.getTwinStatusId())
                .setIncomingElseOutgoing(src.getIncomingElseOutgoing())
                .setOrder(src.getOrder())
                .setTwinTriggerId(src.getTwinTriggerId())
                .setAsync(src.getAsync())
                .setActive(src.getActive() != null ? src.getActive() : true);
    }
}
