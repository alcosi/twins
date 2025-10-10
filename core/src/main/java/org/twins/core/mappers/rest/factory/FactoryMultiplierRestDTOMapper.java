package org.twins.core.mappers.rest.factory;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModeBinding;
import org.twins.core.controller.rest.annotation.MapperModePointerBinding;
import org.twins.core.dao.factory.TwinFactoryMultiplierEntity;
import org.twins.core.dto.rest.factory.FactoryMultiplierDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.featurer.FeaturerRestDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.*;
import org.twins.core.mappers.rest.twinclass.TwinClassBaseRestDTOMapper;
import org.twins.core.service.factory.TwinFactoryService;

import java.util.Collection;

@Component
@RequiredArgsConstructor
@MapperModeBinding(modes = {FactoryMultiplierMode.class, FactoryMultiplierFiltersCountMode.class})
public class FactoryMultiplierRestDTOMapper extends RestSimpleDTOMapper<TwinFactoryMultiplierEntity, FactoryMultiplierDTOv1> {

    private final TwinFactoryService twinFactoryService;

    @MapperModePointerBinding(modes = FactoryMode.FactoryMultiplier2FactoryMode.class)
    private final FactoryRestDTOMapper factoryRestDTOMapper;

    @MapperModePointerBinding(modes = TwinClassMode.FactoryMultiplier2TwinClassMode.class)
    private final TwinClassBaseRestDTOMapper twinClassBaseRestDTOMapper;

    @MapperModePointerBinding(modes = FeaturerMode.FactoryMultiplier2FeaturerMode.class)
    private final FeaturerRestDTOMapper featurerRestDTOMapper;

    @Override
    public void map(TwinFactoryMultiplierEntity src, FactoryMultiplierDTOv1 dst, MapperContext mapperContext) throws Exception {
        switch (mapperContext.getModeOrUse(FactoryMultiplierMode.DETAILED)) {
            case DETAILED -> dst
                    .setId(src.getId())
                    .setFactoryId(src.getTwinFactoryId())
                    .setInputTwinClassId(src.getInputTwinClassId())
                    .setMultiplierFeaturerId(src.getMultiplierFeaturerId())
                    .setMultiplierParams(src.getMultiplierParams())
                    .setDescription(src.getDescription())
                    .setActive(src.getActive());
            case SHORT -> dst
                    .setId(src.getId())
                    .setFactoryId(src.getTwinFactoryId())
                    .setInputTwinClassId(src.getInputTwinClassId());
        }
        if (mapperContext.hasModeButNot(FactoryMultiplierFiltersCountMode.HIDE)) {
            twinFactoryService.countFactoryMultiplierFilters(src);
            dst.setFactoryMultiplierFiltersCount(src.getFactoryMultiplierFiltersCount());
        }
        if (mapperContext.hasModeButNot(FactoryMode.FactoryMultiplier2FactoryMode.HIDE)) {
            dst.setFactoryId(src.getTwinFactoryId());
            factoryRestDTOMapper.postpone(src.getTwinFactory(), mapperContext.forkOnPoint(FactoryMode.FactoryMultiplier2FactoryMode.SHORT));
        }
        if (mapperContext.hasModeButNot(TwinClassMode.FactoryMultiplier2TwinClassMode.HIDE)) {
            dst.setInputTwinClassId(src.getInputTwinClassId());
            twinClassBaseRestDTOMapper.postpone(src.getInputTwinClass(), mapperContext.forkOnPoint(TwinClassMode.FactoryMultiplier2TwinClassMode.SHORT));
        }
        if (mapperContext.hasModeButNot(FeaturerMode.FactoryMultiplier2FeaturerMode.HIDE)) {
            dst.setMultiplierFeaturerId(src.getMultiplierFeaturerId());
            featurerRestDTOMapper.postpone(src.getMultiplierFeaturer(), mapperContext.forkOnPoint(FeaturerMode.FactoryMultiplier2FeaturerMode.SHORT));
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
