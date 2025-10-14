package org.twins.core.mappers.rest.factory;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModeBinding;
import org.twins.core.controller.rest.annotation.MapperModePointerBinding;
import org.twins.core.dao.factory.TwinFactoryEraserEntity;
import org.twins.core.dto.rest.factory.FactoryEraserDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.FactoryConditionSetMode;
import org.twins.core.mappers.rest.mappercontext.modes.FactoryEraserMode;
import org.twins.core.mappers.rest.mappercontext.modes.FactoryMode;
import org.twins.core.mappers.rest.mappercontext.modes.TwinClassMode;
import org.twins.core.mappers.rest.twinclass.TwinClassRestDTOMapper;

@Component
@RequiredArgsConstructor
@MapperModeBinding(modes = FactoryEraserMode.class)
public class FactoryEraserRestDTOMapper extends RestSimpleDTOMapper<TwinFactoryEraserEntity, FactoryEraserDTOv1> {

    @MapperModePointerBinding(modes = FactoryMode.FactoryEraser2FactoryMode.class)
    private final FactoryRestDTOMapper factoryRestDTOMapper;

    @MapperModePointerBinding(modes = FactoryConditionSetMode.FactoryEraser2FactoryConditionSetMode.class)
    private final FactoryConditionSetRestDTOMapper factoryConditionSetRestDTOMapper;

    @MapperModePointerBinding(modes = TwinClassMode.FactoryEraser2TwinClassMode.class)
    private final TwinClassRestDTOMapper twinClassRestDTOMapper;

    @Override
    public void map(TwinFactoryEraserEntity src, FactoryEraserDTOv1 dst, MapperContext mapperContext) throws Exception {
        switch (mapperContext.getModeOrUse(FactoryEraserMode.DETAILED)) {
            case DETAILED ->
                dst
                        .setId(src.getId())
                        .setFactoryId(src.getTwinFactoryId())
                        .setInputTwinClassId(src.getInputTwinClassId())
                        .setFactoryConditionSetId(src.getTwinFactoryConditionSetId())
                        .setFactoryConditionSetInvert(src.getTwinFactoryConditionInvert())
                        .setDescription(src.getDescription())
                        .setAction(src.getEraserAction())
                        .setActive(src.getActive());
            case SHORT ->
                dst
                        .setId(src.getId())
                        .setFactoryId(src.getTwinFactoryId())
                        .setAction(src.getEraserAction());
        }
        if (mapperContext.hasModeButNot(FactoryMode.FactoryEraser2FactoryMode.HIDE)) {
            dst.setFactoryId(src.getTwinFactoryId());
            factoryRestDTOMapper.postpone(src.getTwinFactory(), mapperContext.forkOnPoint(FactoryMode.FactoryEraser2FactoryMode.SHORT));
        }
        if (mapperContext.hasModeButNot(FactoryConditionSetMode.FactoryEraser2FactoryConditionSetMode.HIDE)) {
            dst.setFactoryConditionSetId(src.getTwinFactoryConditionSetId());
            factoryConditionSetRestDTOMapper.postpone(src.getConditionSet(), mapperContext.forkOnPoint(FactoryConditionSetMode.FactoryEraser2FactoryConditionSetMode.SHORT));
        }
        if (mapperContext.hasModeButNot(TwinClassMode.FactoryEraser2TwinClassMode.HIDE)) {
            dst.setInputTwinClassId(src.getInputTwinClassId());
            twinClassRestDTOMapper.postpone(src.getInputTwinClass(), mapperContext.forkOnPoint(TwinClassMode.FactoryEraser2TwinClassMode.SHORT));
        }
    }
}
