package org.twins.core.mappers.rest.factory;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModeBinding;
import org.twins.core.controller.rest.annotation.MapperModePointerBinding;
import org.twins.core.dao.factory.TwinFactoryConditionEntity;
import org.twins.core.domain.CountResult;
import org.twins.core.dto.rest.factory.FactoryConditionCountDTOv1;
import org.twins.core.enums.sort.FactoryConditionGroupField;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.featurer.FeaturerRestDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.FactoryConditionSetMode;
import org.twins.core.mappers.rest.mappercontext.modes.FeaturerMode;
import org.twins.core.service.factory.FactoryConditionService;

import java.util.Collection;
import java.util.Objects;

@Component
@RequiredArgsConstructor
@MapperModeBinding(modes = {FactoryConditionSetMode.class, FeaturerMode.class})
public class FactoryConditionCountRestDTOMapper extends RestSimpleDTOMapper<CountResult<TwinFactoryConditionEntity, FactoryConditionGroupField>, FactoryConditionCountDTOv1> {

    @MapperModePointerBinding(modes = FactoryConditionSetMode.FactoryCondition2FactoryConditionSetMode.class)
    private final FactoryConditionSetRestDTOMapper factoryConditionSetRestDTOMapper;

    @MapperModePointerBinding(modes = FeaturerMode.FactoryCondition2FeaturerMode.class)
    private final FeaturerRestDTOMapper featurerRestDTOMapper;

    private final FactoryConditionService factoryConditionService;

    @Override
    public void map(CountResult<TwinFactoryConditionEntity, FactoryConditionGroupField> src, FactoryConditionCountDTOv1 dst, MapperContext mapperContext) throws Exception {
        var entity = src.getEntity();
        if (entity == null) {
            dst.setCount(src.getCount());
            return;
        }
        dst
                .setFactoryConditionSetId(entity.getTwinFactoryConditionSetId())
                .setConditionerFeaturerId(entity.getConditionerFeaturerId())
                .setInvert(entity.getInvert())
                .setActive(entity.getActive())
                .setCount(src.getCount());
        if (needLoad(mapperContext, FactoryConditionSetMode.FactoryCondition2FactoryConditionSetMode.HIDE, src, FactoryConditionGroupField.factoryConditionSetId)) {
            factoryConditionService.loadConditionSet(entity);
            factoryConditionSetRestDTOMapper.convertOrPostpone(entity.getConditionSet(), mapperContext.forkOnPoint(mapperContext.getModeOrUse(FactoryConditionSetMode.FactoryCondition2FactoryConditionSetMode.SHORT)));
        }
        if (needLoad(mapperContext, FeaturerMode.FactoryCondition2FeaturerMode.HIDE, src, FactoryConditionGroupField.conditionerFeaturerId)) {
            featurerRestDTOMapper.postpone(entity.getConditionerFeaturerId(), mapperContext.forkOnPoint(mapperContext.getModeOrUse(FeaturerMode.FactoryCondition2FeaturerMode.SHORT)));
        }
    }

    @Override
    public void beforeCollectionConversion(Collection<CountResult<TwinFactoryConditionEntity, FactoryConditionGroupField>> srcCollection, MapperContext mapperContext) throws Exception {
        if (srcCollection.isEmpty()) {
            return;
        }
        var entityCollection = srcCollection.stream().map(CountResult::getEntity).filter(Objects::nonNull).toList();
        if (entityCollection.isEmpty()) {
            return;
        }
        var someCount = srcCollection.iterator().next();
        if (needLoad(mapperContext, FactoryConditionSetMode.FactoryCondition2FactoryConditionSetMode.HIDE, someCount, FactoryConditionGroupField.factoryConditionSetId)) {
            factoryConditionService.loadConditionSet(entityCollection);
        }
    }
}
