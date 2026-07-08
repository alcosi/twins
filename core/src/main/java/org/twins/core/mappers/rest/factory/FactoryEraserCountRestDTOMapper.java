package org.twins.core.mappers.rest.factory;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModeBinding;
import org.twins.core.controller.rest.annotation.MapperModePointerBinding;
import org.twins.core.dao.factory.TwinFactoryEraserEntity;
import org.twins.core.domain.CountResult;
import org.twins.core.dto.rest.factory.FactoryEraserCountDTOv1;
import org.twins.core.enums.sort.FactoryEraserGroupField;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.FactoryConditionSetMode;
import org.twins.core.mappers.rest.mappercontext.modes.FactoryMode;
import org.twins.core.mappers.rest.mappercontext.modes.TwinClassMode;
import org.twins.core.mappers.rest.twinclass.TwinClassRestDTOMapper;
import org.twins.core.service.factory.FactoryEraserService;

import java.util.Collection;
import java.util.Objects;

@Component
@RequiredArgsConstructor
@MapperModeBinding(modes = {FactoryMode.class, FactoryConditionSetMode.class, TwinClassMode.class})
public class FactoryEraserCountRestDTOMapper extends RestSimpleDTOMapper<CountResult<TwinFactoryEraserEntity, FactoryEraserGroupField>, FactoryEraserCountDTOv1> {

    @MapperModePointerBinding(modes = FactoryMode.FactoryEraser2FactoryMode.class)
    private final FactoryRestDTOMapper factoryRestDTOMapper;

    @MapperModePointerBinding(modes = FactoryConditionSetMode.FactoryEraser2FactoryConditionSetMode.class)
    private final FactoryConditionSetRestDTOMapper factoryConditionSetRestDTOMapper;

    @MapperModePointerBinding(modes = TwinClassMode.FactoryEraser2TwinClassMode.class)
    private final TwinClassRestDTOMapper twinClassRestDTOMapper;

    private final FactoryEraserService factoryEraserService;

    @Override
    public void map(CountResult<TwinFactoryEraserEntity, FactoryEraserGroupField> src, FactoryEraserCountDTOv1 dst, MapperContext mapperContext) throws Exception {
        var entity = src.getEntity();
        if (entity == null) {
            dst.setCount(src.getCount());
            return;
        }
        dst
                .setFactoryId(entity.getTwinFactoryId())
                .setInputTwinClassId(entity.getInputTwinClassId())
                .setFactoryConditionSetId(entity.getTwinFactoryConditionSetId())
                .setFactoryConditionSetInvert(entity.getTwinFactoryConditionInvert())
                .setActive(entity.getActive())
                .setAction(entity.getEraserAction())
                .setCount(src.getCount());
        if (needLoad(mapperContext, FactoryMode.FactoryEraser2FactoryMode.HIDE, src, FactoryEraserGroupField.factoryId)) {
            factoryRestDTOMapper.convertOrPostpone(entity.getTwinFactory(), mapperContext.forkOnPoint(mapperContext.getModeOrUse(FactoryMode.FactoryEraser2FactoryMode.SHORT)));
        }
        if (needLoad(mapperContext, TwinClassMode.FactoryEraser2TwinClassMode.HIDE, src, FactoryEraserGroupField.inputTwinClassId)) {
            twinClassRestDTOMapper.convertOrPostpone(entity.getInputTwinClass(), mapperContext.forkOnPoint(mapperContext.getModeOrUse(TwinClassMode.FactoryEraser2TwinClassMode.SHORT)));
        }
        if (needLoad(mapperContext, FactoryConditionSetMode.FactoryEraser2FactoryConditionSetMode.HIDE, src, FactoryEraserGroupField.factoryConditionSetId)) {
            factoryEraserService.loadConditionSet(entity);
            factoryConditionSetRestDTOMapper.convertOrPostpone(entity.getConditionSet(), mapperContext.forkOnPoint(mapperContext.getModeOrUse(FactoryConditionSetMode.FactoryEraser2FactoryConditionSetMode.SHORT)));
        }
    }

    @Override
    public void beforeCollectionConversion(Collection<CountResult<TwinFactoryEraserEntity, FactoryEraserGroupField>> srcCollection, MapperContext mapperContext) throws Exception {
        if (srcCollection.isEmpty()) {
            return;
        }
        var entityCollection = srcCollection.stream().map(CountResult::getEntity).filter(Objects::nonNull).toList();
        if (entityCollection.isEmpty()) {
            return;
        }
        var someCount = srcCollection.iterator().next();
        if (needLoad(mapperContext, FactoryConditionSetMode.FactoryEraser2FactoryConditionSetMode.HIDE, someCount, FactoryEraserGroupField.factoryConditionSetId)) {
            factoryEraserService.loadConditionSet(entityCollection);
        }
    }
}
