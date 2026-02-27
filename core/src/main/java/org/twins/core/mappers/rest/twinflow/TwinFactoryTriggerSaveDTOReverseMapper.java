package org.twins.core.mappers.rest.twinflow;

import org.springframework.stereotype.Component;
import org.twins.core.dao.factory.TwinFactoryTriggerEntity;
import org.twins.core.dto.rest.twinflow.TwinFactoryTriggerSaveDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
public class TwinFactoryTriggerSaveDTOReverseMapper extends RestSimpleDTOMapper<TwinFactoryTriggerSaveDTOv1, TwinFactoryTriggerEntity> {

    @Override
    public void map(TwinFactoryTriggerSaveDTOv1 src, TwinFactoryTriggerEntity dst, MapperContext mapperContext) throws Exception {
        dst
                .setTwinFactoryId(src.getTwinFactoryId())
                .setInputTwinClassId(src.getInputTwinClassId())
                .setTwinFactoryConditionSetId(src.getTwinFactoryConditionSetId())
                .setTwinFactoryConditionInvert(src.getTwinFactoryConditionInvert())
                .setActive(src.getActive())
                .setDescription(src.getDescription())
                .setTwinTriggerId(src.getTwinTriggerId())
                .setAsync(src.getAsync());
    }
}
