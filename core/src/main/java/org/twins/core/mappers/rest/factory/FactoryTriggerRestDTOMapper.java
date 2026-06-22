package org.twins.core.mappers.rest.factory;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModeBinding;
import org.twins.core.dao.factory.TwinFactoryTriggerEntity;
import org.twins.core.dto.rest.factory.FactoryTriggerDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.FactoryBranchMode;

@Component
@MapperModeBinding(modes = FactoryBranchMode.class)
public class FactoryTriggerRestDTOMapper extends RestSimpleDTOMapper<TwinFactoryTriggerEntity, FactoryTriggerDTOv1> {

    @Override
    public void map(TwinFactoryTriggerEntity src, FactoryTriggerDTOv1 dst, MapperContext mapperContext) throws Exception {
        switch (mapperContext.getModeOrUse(FactoryBranchMode.DETAILED)) {
            case DETAILED ->
                dst
                        .setId(src.getId())
                        .setFactoryId(src.getTwinFactoryId())
                        .setInputTwinClassId(src.getInputTwinClassId())
                        .setTwinTriggerId(src.getTwinTriggerId())
                        .setTwinFactoryConditionSetId(src.getTwinFactoryConditionSetId())
                        .setTwinFactoryConditionInvert(src.getTwinFactoryConditionInvert())
                        .setActive(src.getActive())
                        .setAsync(src.getAsync())
                        .setDescription(src.getDescription());
            case SHORT ->
                dst
                        .setId(src.getId())
                        .setFactoryId(src.getTwinFactoryId())
                        .setInputTwinClassId(src.getInputTwinClassId())
                        .setTwinTriggerId(src.getTwinTriggerId());
        }
    }
}
