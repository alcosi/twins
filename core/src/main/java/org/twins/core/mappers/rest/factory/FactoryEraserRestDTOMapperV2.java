package org.twins.core.mappers.rest.factory;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModePointerBinding;
import org.twins.core.dao.factory.TwinFactoryEraserEntity;
import org.twins.core.dto.rest.factory.FactoryEraserDTOv2;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.FactoryConditionSetMode;
import org.twins.core.mappers.rest.mappercontext.modes.FactoryMode;
import org.twins.core.mappers.rest.mappercontext.modes.TwinClassMode;
import org.twins.core.mappers.rest.permission.FactoryConditionSetRestDTOMapper;
import org.twins.core.mappers.rest.twinclass.TwinClassBaseRestDTOMapper;

@Component
@RequiredArgsConstructor
public class FactoryEraserRestDTOMapperV2 extends RestSimpleDTOMapper<TwinFactoryEraserEntity, FactoryEraserDTOv2> {

    private final FactoryEraserRestDTOMapper factoryEraserRestDTOMapper;

    @MapperModePointerBinding(modes = FactoryMode.FactoryEraser2FactoryMode.class)
    private final FactoryRestDTOMapper factoryRestDTOMapper;

    @MapperModePointerBinding(modes = FactoryConditionSetMode.FactoryEraser2FactoryConditionSetMode.class)
    private final FactoryConditionSetRestDTOMapper factoryConditionSetRestDTOMapper;

    @MapperModePointerBinding(modes = TwinClassMode.FactoryEraser2TwinClassMode.class)
    private final TwinClassBaseRestDTOMapper twinClassBaseRestDTOMapper;

    @Override
    public void map(TwinFactoryEraserEntity src, FactoryEraserDTOv2 dst, MapperContext mapperContext) throws Exception {
        factoryEraserRestDTOMapper.map(src, dst, mapperContext);
        if (mapperContext.hasModeButNot(FactoryMode.FactoryEraser2FactoryMode.HIDE))
            dst
                    .setFactory(factoryRestDTOMapper.convertOrPostpone(src.getTwinFactory(), mapperContext.forkOnPoint(FactoryMode.FactoryEraser2FactoryMode.SHORT)))
                    .setFactoryId(src.getTwinFactoryId());
        if (mapperContext.hasModeButNot(FactoryConditionSetMode.FactoryEraser2FactoryConditionSetMode.HIDE))
            dst
                    .setFactoryConditionSet(factoryConditionSetRestDTOMapper.convertOrPostpone(src.getConditionSet(), mapperContext.forkOnPoint(FactoryConditionSetMode.FactoryEraser2FactoryConditionSetMode.SHORT)))
                    .setFactoryConditionSetId(src.getTwinFactoryConditionSetId());
        if (mapperContext.hasModeButNot(TwinClassMode.FactoryEraser2TwinClassMode.HIDE))
            dst
                    .setInputTwinClass(twinClassBaseRestDTOMapper.convertOrPostpone(src.getInputTwinClass(), mapperContext.forkOnPoint(TwinClassMode.FactoryEraser2TwinClassMode.SHORT)))
                    .setInputTwinClassId(src.getInputTwinClassId());

    }
}
