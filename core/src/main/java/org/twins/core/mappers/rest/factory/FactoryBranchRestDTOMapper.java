package org.twins.core.mappers.rest.factory;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModeBinding;
import org.twins.core.controller.rest.annotation.MapperModePointerBinding;
import org.twins.core.dao.factory.TwinFactoryBranchEntity;
import org.twins.core.dto.rest.factory.FactoryBranchDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.FactoryBranchMode;
import org.twins.core.mappers.rest.mappercontext.modes.FactoryConditionSetMode;
import org.twins.core.mappers.rest.mappercontext.modes.FactoryMode;

@Component
@RequiredArgsConstructor
@MapperModeBinding(modes = FactoryBranchMode.class)
public class FactoryBranchRestDTOMapper extends RestSimpleDTOMapper<TwinFactoryBranchEntity, FactoryBranchDTOv1> {

    @MapperModePointerBinding(modes = FactoryMode.FactoryBranch2FactoryMode.class)
    private final FactoryRestDTOMapper factoryRestDTOMapper;

    @MapperModePointerBinding(modes = FactoryConditionSetMode.FactoryBranch2FactoryConditionSetMode.class)
    private final FactoryConditionSetRestDTOMapper factoryConditionSetRestDTOMapper;

    @Override
    public void map(TwinFactoryBranchEntity src, FactoryBranchDTOv1 dst, MapperContext mapperContext) throws Exception {
        switch (mapperContext.getModeOrUse(FactoryBranchMode.DETAILED)) {
            case DETAILED ->
                dst
                        .setId(src.getId())
                        .setFactoryId(src.getTwinFactoryId())
                        .setFactoryConditionSetId(src.getTwinFactoryConditionSetId())
                        .setFactoryConditionSetInvert(src.getTwinFactoryConditionInvert())
                        .setNextFactoryId(src.getNextTwinFactoryId())
                        .setDescription(src.getDescription())
                        .setActive(src.getActive());
            case SHORT ->
                dst
                        .setId(src.getId())
                        .setFactoryId(src.getTwinFactoryId());

        }
        if (mapperContext.hasModeButNot(FactoryMode.FactoryBranch2FactoryMode.HIDE)) {
            dst
                    .setFactoryId(src.getTwinFactoryId())
                    .setNextFactoryId(src.getNextTwinFactoryId());
            factoryRestDTOMapper.postpone(src.getFactory(), mapperContext.forkOnPoint(FactoryMode.FactoryBranch2FactoryMode.SHORT));
            factoryRestDTOMapper.postpone(src.getNextFactory(), mapperContext.forkOnPoint(FactoryMode.FactoryBranch2FactoryMode.SHORT));
        }
        if (mapperContext.hasModeButNot(FactoryConditionSetMode.FactoryBranch2FactoryConditionSetMode.HIDE)) {
            dst
                    .setFactoryConditionSetId(src.getTwinFactoryConditionSetId());
            factoryConditionSetRestDTOMapper.postpone(src.getConditionSet(), mapperContext.forkOnPoint(FactoryConditionSetMode.FactoryBranch2FactoryConditionSetMode.SHORT));
        }
    }
}
