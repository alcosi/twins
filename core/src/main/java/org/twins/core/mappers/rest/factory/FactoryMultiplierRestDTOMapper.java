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
import org.twins.core.mappers.rest.twinclass.TwinClassRestDTOMapper;
import org.twins.core.service.factory.FactoryMultiplierService;
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
    private final TwinClassRestDTOMapper twinClassRestDTOMapper;

    @MapperModePointerBinding(modes = FeaturerMode.FactoryMultiplier2FeaturerMode.class)
    private final FeaturerRestDTOMapper featurerRestDTOMapper;
    private final FactoryMultiplierService factoryMultiplierService;

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
            twinClassRestDTOMapper.postpone(src.getInputTwinClass(), mapperContext.forkOnPoint(TwinClassMode.FactoryMultiplier2TwinClassMode.SHORT));
        }
        if (mapperContext.hasModeButNot(FeaturerMode.FactoryMultiplier2FeaturerMode.HIDE)) {
            dst.setMultiplierFeaturerId(src.getMultiplierFeaturerId());
            factoryMultiplierService.loadMultiplier(src);
            featurerRestDTOMapper.postpone(src.getMultiplierFeaturer(), mapperContext.forkOnPoint(FeaturerMode.FactoryMultiplier2FeaturerMode.SHORT));
        }
    }

    @Override
    public void beforeCollectionConversion(Collection<TwinFactoryMultiplierEntity> srcCollection, MapperContext mapperContext) throws Exception {
        super.beforeCollectionConversion(srcCollection, mapperContext);
        if (mapperContext.hasMode(FactoryMultiplierFiltersCountMode.SHOW)) {
            twinFactoryService.countFactoryMultiplierFilters(srcCollection);
        }
        if (mapperContext.hasModeButNot(FeaturerMode.FactoryMultiplier2FeaturerMode.HIDE)) {
            factoryMultiplierService.loadMultipliers(srcCollection);
        }
    }
}
