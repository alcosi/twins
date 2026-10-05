package org.twins.core.mappers.rest.twinstatus;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinStatusTriggerEntity;
import org.twins.core.dto.rest.twinstatus.TwinStatusTriggerCreateDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
@RequiredArgsConstructor
public class TwinStatusTriggerCreateDTOReverseMapper extends RestSimpleDTOMapper<TwinStatusTriggerCreateDTOv1, TwinStatusTriggerEntity> {

    @Override
    public void map(TwinStatusTriggerCreateDTOv1 src, TwinStatusTriggerEntity dst, MapperContext mapperContext) throws Exception {
        dst
                .setTwinStatusId(src.getTwinStatusId())
                .setIncomingElseOutgoing(src.getIncomingElseOutgoing())
                .setOrder(src.getOrder())
                .setTwinTriggerId(src.getTwinTriggerId())
                .setAsync(src.getAsync())
                .setActive(src.getActive() != null ? src.getActive() : true);
    }
}
