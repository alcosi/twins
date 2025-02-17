package org.twins.core.mappers.rest.factory;

import lombok.RequiredArgsConstructor;
import org.cambium.i18n.service.I18nService;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModeBinding;
import org.twins.core.dao.factory.TwinFactoryEntity;
import org.twins.core.dto.rest.factory.FactoryDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.*;
import org.twins.core.service.factory.TwinFactoryService;

import java.util.Collection;

@Component
@RequiredArgsConstructor
@MapperModeBinding(modes = {
        FactoryMode.class,
        FactoryUsagesCountMode.class,
        FactoryPipelineCountMode.class,
        FactoryMultipliersCountMode.class,
        FactoryBranchesCountMode.class,
        FactoryErasersCountMode.class})
public class FactoryRestDTOMapper extends RestSimpleDTOMapper<TwinFactoryEntity, FactoryDTOv1> {

    private final I18nService i18nService;
    private final TwinFactoryService twinFactoryService;

    @Override
    public void map(TwinFactoryEntity src, FactoryDTOv1 dst, MapperContext mapperContext) throws Exception {
        switch (mapperContext.getModeOrUse(FactoryMode.DETAILED)) {
            case DETAILED:
                dst
                        .setId(src.getId())
                        .setKey(src.getKey())
                        .setName(src.getNameI18NId() != null ? i18nService.translateToLocale(src.getNameI18NId()) : "")
                        .setDescription(src.getDescriptionI18NId() != null ? i18nService.translateToLocale(src.getDescriptionI18NId()) : "")
                        .setCreatedAt(src.getCreatedAt().toLocalDateTime())
                        .setCreatedByUserId(src.getCreatedByUserId());
                break;
            case SHORT:
                dst
                        .setId(src.getId())
                        .setKey(src.getKey())
                        .setCreatedByUserId(src.getCreatedByUserId());
                break;
        }
        if (showFactoryUsagesCount(mapperContext)) {
            twinFactoryService.countFactoryUsages(src);
            dst.setId(src.getId()).setFactoryUsagesCount(src.getFactoryUsagesCount());
        }
        if (showFactoryPipelinesCount(mapperContext)) {
            twinFactoryService.countFactoryPipelines(src);
            dst.setId(src.getId()).setFactoryPipelinesCount(src.getFactoryPipelinesCount());
        }
        if (showFactoryMultipliersCount(mapperContext)) {
            twinFactoryService.countFactoryMultipliers(src);
            dst.setId(src.getId()).setFactoryMultipliersCount(src.getFactoryMultipliersCount());
        }
        if (showFactoryBranchesCount(mapperContext)) {
            twinFactoryService.countFactoryBranches(src);
            dst.setId(src.getId()).setFactoryBranchesCount(src.getFactoryBranchesCount());
        }
        if (showFactoryErasersCount(mapperContext)) {
            twinFactoryService.countFactoryErasers(src);
            dst.setId(src.getId()).setFactoryErasersCount(src.getFactoryErasersCount());
        }
    }

    private static boolean showFactoryUsagesCount(MapperContext mapperContext) {
        return mapperContext.hasModeButNot(FactoryUsagesCountMode.HIDE);
    }

    private static boolean showFactoryPipelinesCount(MapperContext mapperContext) {
        return mapperContext.hasModeButNot(FactoryPipelineCountMode.HIDE);
    }

    private static boolean showFactoryMultipliersCount(MapperContext mapperContext) {
        return mapperContext.hasModeButNot(FactoryMultipliersCountMode.HIDE);
    }

    private static boolean showFactoryBranchesCount(MapperContext mapperContext) {
        return mapperContext.hasModeButNot(FactoryBranchesCountMode.HIDE);
    }

    private static boolean showFactoryErasersCount(MapperContext mapperContext) {
        return mapperContext.hasModeButNot(FactoryErasersCountMode.HIDE);
    }

    @Override
    public void beforeCollectionConversion(Collection<TwinFactoryEntity> srcCollection, MapperContext mapperContext) throws Exception {
        super.beforeCollectionConversion(srcCollection, mapperContext);
        if (showFactoryUsagesCount(mapperContext))
            twinFactoryService.countFactoryUsages(srcCollection);
        if (showFactoryPipelinesCount(mapperContext))
            twinFactoryService.countFactoryPipelines(srcCollection);
        if (showFactoryMultipliersCount(mapperContext))
            twinFactoryService.countFactoryMultipliers(srcCollection);
        if (showFactoryBranchesCount(mapperContext))
            twinFactoryService.countFactoryBranches(srcCollection);
        if (showFactoryErasersCount(mapperContext))
            twinFactoryService.countFactoryErasers(srcCollection);
    }
}
