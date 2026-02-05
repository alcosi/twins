package org.twins.core.mappers.rest.twinflow;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twinflow.TwinflowTransitionTriggerEntity;
import org.twins.core.dto.rest.twinflow.TriggerBaseDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.TransitionMode;

@Component
@RequiredArgsConstructor
public class TriggerBaseV1RestDTOMapper extends RestSimpleDTOMapper<TwinflowTransitionTriggerEntity, TriggerBaseDTOv1> {

    @Override
    public void map(TwinflowTransitionTriggerEntity src, TriggerBaseDTOv1 dst, MapperContext mapperContext) throws Exception {
                dst
                        .setOrder(src.getOrder())
                        .setTwinTriggerId(src.getTwinTriggerId())
                        .setAsync(src.getAsync())
                        .setActive(src.getIsActive());
    }

    @Override
    public String getObjectCacheId(TwinflowTransitionTriggerEntity src) {
        return src.getId().toString();
    }

    @Override
    public boolean hideMode(MapperContext mapperContext) {
        return mapperContext.hasModeOrEmpty(TransitionMode.HIDE);
    }

}
