package org.twins.core.mappers.rest.twinstatus;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModeBinding;
import org.twins.core.dao.twin.TwinStatusTransitionTriggerEntity;
import org.twins.core.dto.rest.twinstatus.TwinStatusTransitionTriggerDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.TwinStatusTransitionTriggerMode;

@Component
@RequiredArgsConstructor
@MapperModeBinding(modes = TwinStatusTransitionTriggerMode.class)
public class TwinStatusTransitionTriggerRestDTOMapper extends RestSimpleDTOMapper<TwinStatusTransitionTriggerEntity, TwinStatusTransitionTriggerDTOv1> {

    @Override
    public void map(TwinStatusTransitionTriggerEntity src, TwinStatusTransitionTriggerDTOv1 dst, MapperContext mapperContext) throws Exception {
        switch (mapperContext.getModeOrUse(TwinStatusTransitionTriggerMode.DETAILED)) {
            case DETAILED -> dst
                    .setId(src.getId())
                    .setTwinStatusId(src.getTwinStatusId())
                    .setType(src.getType())
                    .setOrder(src.getOrder())
                    .setTwinTriggerId(src.getTwinTriggerId())
                    .setAsync(src.getAsync())
                    .setActive(src.getActive());
            case SHORT -> dst
                    .setId(src.getId())
                    .setTwinStatusId(src.getTwinStatusId())
                    .setType(src.getType())
                    .setTwinTriggerId(src.getTwinTriggerId());
        }
    }

    @Override
    public boolean hideMode(MapperContext mapperContext) {
        return mapperContext.hasModeOrEmpty(TwinStatusTransitionTriggerMode.HIDE);
    }

    @Override
    public String getObjectCacheId(TwinStatusTransitionTriggerEntity src) {
        return src.getId().toString();
    }
}
