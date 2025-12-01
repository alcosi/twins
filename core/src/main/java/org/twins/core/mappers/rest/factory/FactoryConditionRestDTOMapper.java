package org.twins.core.mappers.rest.factory;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModeBinding;
import org.twins.core.controller.rest.annotation.MapperModePointerBinding;
import org.twins.core.dao.factory.TwinFactoryConditionEntity;
import org.twins.core.dto.rest.factory.FactoryConditionDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.FactoryConditionMode;
import org.twins.core.mappers.rest.mappercontext.modes.FactoryConditionSetMode;

@Component
@RequiredArgsConstructor
@MapperModeBinding(modes = FactoryConditionMode.class)
public class FactoryConditionRestDTOMapper extends RestSimpleDTOMapper<TwinFactoryConditionEntity, FactoryConditionDTOv1> {


    @MapperModePointerBinding(modes = FactoryConditionSetMode.FactoryCondition2FactoryConditionSetMode.class)
    private final FactoryConditionSetRestDTOMapper factoryConditionSetRestDTOMapper;

    @Override
    public void map(TwinFactoryConditionEntity src, FactoryConditionDTOv1 dst, MapperContext mapperContext) throws Exception {
        switch (mapperContext.getModeOrUse(FactoryConditionMode.DETAILED)) {
            case DETAILED ->
                    dst
                            .setId(src.getId())
                            .setFactoryConditionSetId(src.getTwinFactoryConditionSetId())
                            .setConditionerFeaturerId(src.getConditionerFeaturerId())
                            .setConditionerParams(src.getConditionerParams())
                            .setDescription(src.getDescription())
                            .setActive(src.isActive())
                            .setInvert(src.isInvert());
            case SHORT ->
                    dst
                            .setId(src.getId());
        }
        if (mapperContext.hasModeButNot(FactoryConditionSetMode.FactoryCondition2FactoryConditionSetMode.HIDE)) {
            dst
                    .setFactoryConditionSetId(src.getTwinFactoryConditionSetId());
            factoryConditionSetRestDTOMapper.postpone(src.getConditionSet(), mapperContext.forkOnPoint(FactoryConditionSetMode.FactoryCondition2FactoryConditionSetMode.SHORT));
        }
    }


}
