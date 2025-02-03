package org.twins.core.mappers.rest.factory;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModePointerBinding;
import org.twins.core.dao.factory.TwinFactoryBranchEntity;
import org.twins.core.dto.rest.factory.FactoryBranchDTOv2;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.FactoryConditionSetMode;
import org.twins.core.mappers.rest.mappercontext.modes.FactoryMode;

@Component
@RequiredArgsConstructor
public class FactoryBranchRestDTOMapperV2 extends RestSimpleDTOMapper<TwinFactoryBranchEntity, FactoryBranchDTOv2> {

    private final FactoryBranchRestDTOMapper factoryBranchRestDTOMapper;

    @MapperModePointerBinding(modes = FactoryMode.FactoryBranch2FactoryMode.class)
    private final FactoryRestDTOMapper factoryRestDTOMapper;

    @MapperModePointerBinding(modes = FactoryConditionSetMode.FactoryBranch2FactoryConditionSetMode.class)
    private final FactoryConditionSetRestDTOMapper factoryConditionSetRestDTOMapper;

    @Override
    public void map(TwinFactoryBranchEntity src, FactoryBranchDTOv2 dst, MapperContext mapperContext) throws Exception {
        factoryBranchRestDTOMapper.map(src, dst, mapperContext);
        if (mapperContext.hasModeButNot(FactoryMode.FactoryBranch2FactoryMode.HIDE))
            dst
                    .setFactory(factoryRestDTOMapper.convertOrPostpone(src.getFactory(), mapperContext.forkOnPoint(FactoryMode.FactoryBranch2FactoryMode.SHORT)))
                    .setNextFactory(factoryRestDTOMapper.convertOrPostpone(src.getNextFactory(), mapperContext.forkOnPoint(FactoryMode.FactoryBranch2FactoryMode.SHORT)))
                    .setFactoryId(src.getTwinFactoryId())
                    .setNextFactoryId(src.getNextTwinFactoryId());
        if (mapperContext.hasModeButNot(FactoryConditionSetMode.FactoryBranch2FactoryConditionSetMode.HIDE))
            dst
                    .setFactoryConditionSet(factoryConditionSetRestDTOMapper.convertOrPostpone(src.getConditionSet(), mapperContext.forkOnPoint(FactoryConditionSetMode.FactoryBranch2FactoryConditionSetMode.SHORT)))
                    .setFactoryConditionSetId(src.getTwinFactoryConditionSetId());
    }
}
