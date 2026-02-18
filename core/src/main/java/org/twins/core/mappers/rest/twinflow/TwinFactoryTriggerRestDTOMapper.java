package org.twins.core.mappers.rest.twinflow;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModeBinding;
import org.twins.core.dao.factory.TwinFactoryTriggerEntity;
import org.twins.core.dto.rest.twinflow.TwinFactoryTriggerDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.TwinFactoryTriggerMode;

@Component
@RequiredArgsConstructor
@MapperModeBinding(modes = TwinFactoryTriggerMode.class)
public class TwinFactoryTriggerRestDTOMapper extends RestSimpleDTOMapper<TwinFactoryTriggerEntity, TwinFactoryTriggerDTOv1> {

    @Override
    public void map(TwinFactoryTriggerEntity src, TwinFactoryTriggerDTOv1 dst, MapperContext mapperContext) throws Exception {
        switch (mapperContext.getModeOrUse(TwinFactoryTriggerMode.DETAILED)) {
            case DETAILED -> dst
                    .setId(src.getId())
                    .setTwinFactoryId(src.getTwinFactoryId())
                    .setInputTwinClassId(src.getInputTwinClassId())
                    .setTwinFactoryConditionSetId(src.getTwinFactoryConditionSetId())
                    .setTwinFactoryConditionInvert(src.getTwinFactoryConditionInvert())
                    .setActive(src.getActive())
                    .setDescription(src.getDescription())
                    .setTwinTriggerId(src.getTwinTriggerId())
                    .setAsync(src.getAsync());
            case SHORT -> dst
                    .setId(src.getId())
                    .setTwinFactoryId(src.getTwinFactoryId())
                    .setInputTwinClassId(src.getInputTwinClassId())
                    .setTwinTriggerId(src.getTwinTriggerId());
        }
    }

    @Override
    public boolean hideMode(MapperContext mapperContext) {
        return mapperContext.hasModeOrEmpty(TwinFactoryTriggerMode.HIDE);
    }

    @Override
    public String getObjectCacheId(TwinFactoryTriggerEntity src) {
        return src.getId().toString();
    }
}
