package org.twins.core.mappers.rest.factory;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModeBinding;
import org.twins.core.controller.rest.annotation.MapperModePointerBinding;
import org.twins.core.dao.factory.TwinFactoryBranchEntity;
import org.twins.core.domain.CountResult;
import org.twins.core.dto.rest.factory.FactoryBranchCountDTOv1;
import org.twins.core.enums.sort.FactoryBranchGroupField;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.FactoryConditionSetMode;
import org.twins.core.mappers.rest.mappercontext.modes.FactoryMode;
import org.twins.core.service.factory.FactoryBranchService;

import java.util.Collection;
import java.util.Objects;

@Component
@RequiredArgsConstructor
@MapperModeBinding(modes = {FactoryMode.class, FactoryConditionSetMode.class})
public class FactoryBranchCountRestDTOMapper extends RestSimpleDTOMapper<CountResult<TwinFactoryBranchEntity, FactoryBranchGroupField>, FactoryBranchCountDTOv1> {

    @MapperModePointerBinding(modes = FactoryMode.FactoryBranch2FactoryMode.class)
    private final FactoryRestDTOMapper factoryRestDTOMapper;

    @MapperModePointerBinding(modes = FactoryConditionSetMode.FactoryBranch2FactoryConditionSetMode.class)
    private final FactoryConditionSetRestDTOMapper factoryConditionSetRestDTOMapper;

    private final FactoryBranchService factoryBranchService;

    @Override
    public void map(CountResult<TwinFactoryBranchEntity, FactoryBranchGroupField> src, FactoryBranchCountDTOv1 dst, MapperContext mapperContext) throws Exception {
        var entity = src.getEntity();
        if (entity == null) {
            dst.setCount(src.getCount());
            return;
        }
        dst
                .setFactoryId(entity.getTwinFactoryId())
                .setFactoryConditionSetId(entity.getTwinFactoryConditionSetId())
                .setNextFactoryId(entity.getNextTwinFactoryId())
                .setActive(entity.getActive())
                .setFactoryConditionSetInvert(entity.getTwinFactoryConditionInvert())
                .setCount(src.getCount());
        if (needLoad(mapperContext, FactoryMode.FactoryBranch2FactoryMode.HIDE, src, FactoryBranchGroupField.factoryId)) {
            factoryBranchService.loadFactory(entity);
            factoryRestDTOMapper.convertOrPostpone(entity.getFactory(), mapperContext.forkOnPoint(mapperContext.getModeOrUse(FactoryMode.FactoryBranch2FactoryMode.SHORT)));
        }
        if (needLoad(mapperContext, FactoryMode.FactoryBranch2FactoryMode.HIDE, src, FactoryBranchGroupField.nextFactoryId)) {
            factoryBranchService.loadNextFactory(entity);
            factoryRestDTOMapper.convertOrPostpone(entity.getNextFactory(), mapperContext.forkOnPoint(mapperContext.getModeOrUse(FactoryMode.FactoryBranch2FactoryMode.SHORT)));
        }
        if (needLoad(mapperContext, FactoryConditionSetMode.FactoryBranch2FactoryConditionSetMode.HIDE, src, FactoryBranchGroupField.factoryConditionSetId)) {
            factoryBranchService.loadConditionSet(entity);
            factoryConditionSetRestDTOMapper.convertOrPostpone(entity.getTwinFactoryConditionSet(), mapperContext.forkOnPoint(mapperContext.getModeOrUse(FactoryConditionSetMode.FactoryBranch2FactoryConditionSetMode.SHORT)));
        }
    }

    @Override
    public void beforeCollectionConversion(Collection<CountResult<TwinFactoryBranchEntity, FactoryBranchGroupField>> srcCollection, MapperContext mapperContext) throws Exception {
        if (srcCollection.isEmpty()) {
            return;
        }
        var entityCollection = srcCollection.stream().map(CountResult::getEntity).filter(Objects::nonNull).toList();
        if (entityCollection.isEmpty()) {
            return;
        }
        var someCount = srcCollection.iterator().next();
        if (needLoad(mapperContext, FactoryMode.FactoryBranch2FactoryMode.HIDE, someCount, FactoryBranchGroupField.factoryId)) {
            factoryBranchService.loadFactory(entityCollection);
        }
        if (needLoad(mapperContext, FactoryMode.FactoryBranch2FactoryMode.HIDE, someCount, FactoryBranchGroupField.nextFactoryId)) {
            factoryBranchService.loadNextFactory(entityCollection);
        }
        if (needLoad(mapperContext, FactoryConditionSetMode.FactoryBranch2FactoryConditionSetMode.HIDE, someCount, FactoryBranchGroupField.factoryConditionSetId)) {
            factoryBranchService.loadConditionSet(entityCollection);
        }
    }
}
