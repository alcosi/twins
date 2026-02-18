package org.twins.core.mappers.rest.trigger;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModeBinding;
import org.twins.core.dao.trigger.TwinTriggerEntity;
import org.twins.core.dto.rest.trigger.TwinTriggerDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.TwinTriggerMode;

@Component
@RequiredArgsConstructor
@MapperModeBinding(modes = TwinTriggerMode.class)
public class TwinTriggerRestDTOMapper extends RestSimpleDTOMapper<TwinTriggerEntity, TwinTriggerDTOv1> {

    @Override
    public void map(TwinTriggerEntity src, TwinTriggerDTOv1 dst, MapperContext mapperContext) throws Exception {
        switch (mapperContext.getModeOrUse(TwinTriggerMode.DETAILED)) {
            case DETAILED -> dst
                    .setId(src.getId())
                    .setTriggerFeaturerId(src.getTwinTriggerFeaturerId())
                    .setTriggerParams(src.getTwinTriggerParam())
                    .setName(src.getName())
                    .setDescription(src.getDescription())
                    .setActive(src.getActive());
            case SHORT -> dst
                    .setId(src.getId())
                    .setName(src.getName());
        }
    }

    @Override
    public boolean hideMode(MapperContext mapperContext) {
        return mapperContext.hasModeOrEmpty(TwinTriggerMode.HIDE);
    }

    @Override
    public String getObjectCacheId(TwinTriggerEntity src) {
        return src.getId().toString();
    }
}
