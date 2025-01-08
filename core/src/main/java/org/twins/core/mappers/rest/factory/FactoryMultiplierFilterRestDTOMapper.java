package org.twins.core.mappers.rest.factory;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModeBinding;
import org.twins.core.dao.factory.TwinFactoryMultiplierFilterEntity;
import org.twins.core.dto.rest.factory.FactoryMultiplierFilterDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.FactoryMultiplierFilterMode;

@Component
@RequiredArgsConstructor
@MapperModeBinding(modes = FactoryMultiplierFilterMode.class)
public class FactoryMultiplierFilterRestDTOMapper extends RestSimpleDTOMapper<TwinFactoryMultiplierFilterEntity, FactoryMultiplierFilterDTOv1> {

    @Override
    public void map(TwinFactoryMultiplierFilterEntity src, FactoryMultiplierFilterDTOv1 dst, MapperContext mapperContext) throws Exception {
        switch (mapperContext.getModeOrUse(FactoryMultiplierFilterMode.DETAILED)) {
            case DETAILED:
                dst
                        .setId(src.getId())
                        .setInputTwinClassId(src.getInputTwinClassId())
                        .setMultiplierId(src.getTwinFactoryMultiplierId())
                        .setFactoryConditionSetId(src.getTwinFactoryConditionSetId())
                        .setFactoryConditionSetInvert(src.isTwinFactoryConditionInvert())
                        .setDescription(src.getDescription())
                        .setActive(src.isActive());
                break;
            case SHORT:
                dst
                        .setId(src.getId())
                        .setMultiplierId(src.getTwinFactoryMultiplierId());
                break;
        }
    }
}
