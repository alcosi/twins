package org.twins.core.mappers.rest.factory;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModeBinding;
import org.twins.core.controller.rest.annotation.MapperModePointerBinding;
import org.twins.core.dao.factory.TwinFactoryMultiplierFilterEntity;
import org.twins.core.dto.rest.factory.FactoryMultiplierFilterDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.FactoryConditionSetMode;
import org.twins.core.mappers.rest.mappercontext.modes.FactoryMultiplierFilterMode;
import org.twins.core.mappers.rest.mappercontext.modes.FactoryMultiplierMode;
import org.twins.core.mappers.rest.mappercontext.modes.TwinClassMode;
import org.twins.core.mappers.rest.twinclass.TwinClassRestDTOMapper;
import org.twins.core.service.factory.FactoryMultiplierFilterService;

import java.util.Collection;

@Component
@RequiredArgsConstructor
@MapperModeBinding(modes = FactoryMultiplierFilterMode.class)
public class FactoryMultiplierFilterRestDTOMapper extends RestSimpleDTOMapper<TwinFactoryMultiplierFilterEntity, FactoryMultiplierFilterDTOv1> {

    private final FactoryMultiplierFilterService factoryMultiplierFilterService;

    @MapperModePointerBinding(modes = FactoryMultiplierMode.FactoryMultiplierFilter2FactoryMultiplierMode.class)
    private final FactoryMultiplierRestDTOMapper factoryMultiplierRestDTOMapper;

    @MapperModePointerBinding(modes = FactoryConditionSetMode.FactoryMultiplierFilter2FactoryConditionSetMode.class)
    private final FactoryConditionSetRestDTOMapper factoryConditionSetRestDTOMapper;

    @MapperModePointerBinding(modes = TwinClassMode.FactoryMultiplierFilter2TwinClassMode.class)
    private final TwinClassRestDTOMapper twinClassRestDTOMapper;

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
        if (mapperContext.hasModeButNot(FactoryMultiplierMode.FactoryMultiplierFilter2FactoryMultiplierMode.HIDE)) {
            dst.setMultiplierId(src.getTwinFactoryMultiplierId());
            factoryMultiplierFilterService.loadMultiplier(src);
            factoryMultiplierRestDTOMapper.postpone(src.getMultiplier(), mapperContext.forkOnPoint(FactoryMultiplierMode.FactoryMultiplierFilter2FactoryMultiplierMode.SHORT));
        }
        if (mapperContext.hasModeButNot(FactoryConditionSetMode.FactoryMultiplierFilter2FactoryConditionSetMode.HIDE)) {
            dst.setFactoryConditionSetId(src.getTwinFactoryConditionSetId());
            factoryMultiplierFilterService.loadConditionSet(src);
            factoryConditionSetRestDTOMapper.postpone(src.getConditionSet(), mapperContext.forkOnPoint(FactoryConditionSetMode.FactoryMultiplierFilter2FactoryConditionSetMode.SHORT));
        }
        if (mapperContext.hasModeButNot(TwinClassMode.FactoryMultiplierFilter2TwinClassMode.HIDE)) {
            dst.setInputTwinClassId(src.getInputTwinClassId());
            factoryMultiplierFilterService.loadInputTwinClass(src);
            twinClassRestDTOMapper.postpone(src.getInputTwinClass(), mapperContext.forkOnPoint(TwinClassMode.FactoryMultiplierFilter2TwinClassMode.SHORT));
        }
    }

    @Override
    public void beforeCollectionConversion(Collection<TwinFactoryMultiplierFilterEntity> srcCollection, MapperContext mapperContext) throws Exception {
        super.beforeCollectionConversion(srcCollection, mapperContext);
        if (srcCollection.isEmpty()) return;
        if (mapperContext.hasModeButNot(FactoryMultiplierMode.FactoryMultiplierFilter2FactoryMultiplierMode.HIDE)) {
            factoryMultiplierFilterService.loadMultiplier(srcCollection);
        }
        if (mapperContext.hasModeButNot(FactoryConditionSetMode.FactoryMultiplierFilter2FactoryConditionSetMode.HIDE)) {
            factoryMultiplierFilterService.loadConditionSet(srcCollection);
        }
        if (mapperContext.hasModeButNot(TwinClassMode.FactoryMultiplierFilter2TwinClassMode.HIDE)) {
            factoryMultiplierFilterService.loadInputTwinClass(srcCollection);
        }
    }
}
