package org.twins.core.mappers.rest.factory;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModeBinding;
import org.twins.core.dao.factory.TwinFactoryEraserEntity;
import org.twins.core.dto.rest.factory.FactoryEraserDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.FactoryEraserMode;

@Component
@RequiredArgsConstructor
@MapperModeBinding(modes = FactoryEraserMode.class)
public class FactoryEraserRestDTOMapper extends RestSimpleDTOMapper<TwinFactoryEraserEntity, FactoryEraserDTOv1> {

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
    }
}
