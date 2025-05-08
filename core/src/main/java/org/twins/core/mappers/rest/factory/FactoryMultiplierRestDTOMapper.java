package org.twins.core.mappers.rest.factory;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModeBinding;
import org.twins.core.dao.factory.TwinFactoryMultiplierEntity;
import org.twins.core.dto.rest.factory.FactoryMultiplierDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.FactoryMultiplierFiltersCountMode;
import org.twins.core.mappers.rest.mappercontext.modes.FactoryMultiplierMode;
import org.twins.core.service.factory.TwinFactoryService;

import java.util.Collection;

@Component
@RequiredArgsConstructor
@MapperModeBinding(modes = {FactoryMultiplierMode.class, FactoryMultiplierFiltersCountMode.class})
public class FactoryMultiplierRestDTOMapper extends RestSimpleDTOMapper<TwinFactoryMultiplierEntity, FactoryMultiplierDTOv1> {

    private final TwinFactoryService twinFactoryService;

    @Override
    public void map(TwinFactoryMultiplierEntity src, FactoryMultiplierDTOv1 dst, MapperContext mapperContext) throws Exception {
        switch (mapperContext.getModeOrUse(FactoryMultiplierMode.DETAILED)) {
            case DETAILED ->
                dst
                        .setId(src.getId())
                        .setFactoryId(src.getTwinFactoryId())
                        .setInputTwinClassId(src.getInputTwinClassId())
                        .setMultiplierFeaturerId(src.getMultiplierFeaturerId())
                        .setMultiplierParams(src.getMultiplierParams())
                        .setDescription(src.getDescription())
                        .setActive(src.getActive());
            case SHORT ->
                dst
                        .setId(src.getId())
                        .setFactoryId(src.getTwinFactoryId())
                        .setInputTwinClassId(src.getInputTwinClassId());
        }
        if (mapperContext.hasModeButNot(FactoryMultiplierFiltersCountMode.HIDE)) {
            twinFactoryService.countFactoryMultiplierFilters(src);
            dst
                    .setId(src.getId())
                    .setFactoryMultiplierFiltersCount(src.getFactoryMultiplierFiltersCount());
        }
    }

    @Override
    public void beforeCollectionConversion(Collection<TwinFactoryMultiplierEntity> srcCollection, MapperContext mapperContext) throws Exception {
        super.beforeCollectionConversion(srcCollection, mapperContext);
        if (mapperContext.hasMode(FactoryMultiplierFiltersCountMode.SHOW)) {
            twinFactoryService.countFactoryMultiplierFilters(srcCollection);
        }
    }
}
