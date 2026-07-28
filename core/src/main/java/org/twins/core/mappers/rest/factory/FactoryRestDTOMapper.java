package org.twins.core.mappers.rest.factory;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModeBinding;
import org.twins.core.controller.rest.annotation.MapperModePointerBinding;
import org.twins.core.dao.factory.TwinFactoryEntity;
import org.twins.core.dto.rest.factory.FactoryDTOv1;
import org.twins.core.holder.I18nCacheHolder;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.*;
import org.twins.core.mappers.rest.user.UserRestDTOMapper;
import org.twins.core.service.factory.FactoryService;
import org.twins.core.service.i18n.I18nService;

import java.util.Collection;

@Component
@RequiredArgsConstructor
@MapperModeBinding(modes = {
        FactoryMode.class,
        FactoryUsagesCountMode.class,
        FactoryPipelineCountMode.class,
        FactoryMultipliersCountMode.class,
        FactoryBranchesCountMode.class,
        FactoryErasersCountMode.class,
        FactoryCascadeMode.class})
public class FactoryRestDTOMapper extends RestSimpleDTOMapper<TwinFactoryEntity, FactoryDTOv1> {

    private final I18nService i18nService;
    private final FactoryService factoryService;

    @MapperModePointerBinding(modes = UserMode.Factory2UserMode.class)
    private final UserRestDTOMapper userRestDTOMapper;

    @Lazy
    @Autowired
    @MapperModePointerBinding(modes = FactoryPipelineMode.Factory2FactoryPipelineMode.class)
    private FactoryPipelineRestDTOMapper factoryPipelineRestDTOMapper;

    @Lazy
    @Autowired
    @MapperModePointerBinding(modes = FactoryBranchMode.Factory2FactoryBranchMode.class)
    private FactoryBranchRestDTOMapper factoryBranchRestDTOMapper;

    @Lazy
    @Autowired
    @MapperModePointerBinding(modes = FactoryMultiplierMode.Factory2FactoryMultiplierMode.class)
    private FactoryMultiplierRestDTOMapper factoryMultiplierRestDTOMapper;

    @Lazy
    @Autowired
    @MapperModePointerBinding(modes = FactoryConditionSetMode.TwinFactory2FactoryConditionSetMode.class)
    private FactoryConditionSetRestDTOMapper factoryConditionSetRestDTOMapper;

    @Lazy
    @Autowired
    @MapperModePointerBinding(modes = FactoryEraserMode.Factory2FactoryEraserMode.class)
    private FactoryEraserRestDTOMapper factoryEraserRestDTOMapper;

    @Lazy
    @Autowired
    @MapperModePointerBinding(modes = FactoryTriggerMode.Factory2FactoryTriggerMode.class)
    private FactoryTriggerRestDTOMapper factoryTriggerRestDTOMapper;

    @Override
    public void map(TwinFactoryEntity src, FactoryDTOv1 dst, MapperContext mapperContext) throws Exception {
        switch (mapperContext.getModeOrUse(FactoryMode.DETAILED)) {
            case DETAILED:
                dst
                        .setId(src.getId())
                        .setKey(src.getKey())
                        .setName(I18nCacheHolder.addId(src.getNameI18NId()))
                        .setDescription(I18nCacheHolder.addId(src.getDescriptionI18NId()))
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
            factoryService.countFactoryUsages(src);
            dst.setId(src.getId()).setFactoryUsagesCount(src.getFactoryUsagesCount());
        }
        if (showFactoryPipelinesCount(mapperContext)) {
            factoryService.countFactoryPipelines(src);
            dst.setId(src.getId()).setFactoryPipelinesCount(src.getFactoryPipelinesCount());
        }
        if (showFactoryMultipliersCount(mapperContext)) {
            factoryService.countFactoryMultipliers(src);
            dst.setId(src.getId()).setFactoryMultipliersCount(src.getFactoryMultipliersCount());
        }
        if (showFactoryBranchesCount(mapperContext)) {
            factoryService.countFactoryBranches(src);
            dst.setId(src.getId()).setFactoryBranchesCount(src.getFactoryBranchesCount());
        }
        if (showFactoryErasersCount(mapperContext)) {
            factoryService.countFactoryErasers(src);
            dst.setId(src.getId()).setFactoryErasersCount(src.getFactoryErasersCount());
        }
        if (mapperContext.hasModeButNot(UserMode.Factory2UserMode.HIDE)) {
            dst.setCreatedByUserId(src.getCreatedByUserId());
            factoryService.loadCreatedByUser(src);
            userRestDTOMapper.postpone(src.getCreatedByUser(), mapperContext.forkOnPoint(UserMode.Factory2UserMode.SHORT));
        }
        boolean showPipelines = mapperContext.hasModeButNot(FactoryPipelineMode.Factory2FactoryPipelineMode.HIDE);
        boolean showBranches = mapperContext.hasModeButNot(FactoryBranchMode.Factory2FactoryBranchMode.HIDE);
        boolean showMultipliers = mapperContext.hasModeButNot(FactoryMultiplierMode.Factory2FactoryMultiplierMode.HIDE);
        boolean showConditionSets = mapperContext.hasModeButNot(FactoryConditionSetMode.TwinFactory2FactoryConditionSetMode.HIDE);
        boolean showErasers = mapperContext.hasModeButNot(FactoryEraserMode.Factory2FactoryEraserMode.HIDE);
        boolean showTriggers = mapperContext.hasModeButNot(FactoryTriggerMode.Factory2FactoryTriggerMode.HIDE);
        if (showPipelines || showBranches || showMultipliers || showConditionSets || showErasers || showTriggers) {
            factoryService.loadFactoryElements(src);
        }
        if (showPipelines) {
            dst.setPipelineIdList(src.getTwinFactoryPipelineKit().getIdSet());
            factoryPipelineRestDTOMapper.postpone(src.getTwinFactoryPipelineKit(), mapperContext.forkOnPoint(FactoryPipelineMode.Factory2FactoryPipelineMode.SHORT));
        }
        if (showBranches) {
            dst.setBranchIdList(src.getTwinFactoryBranchKit().getIdSet());
            factoryBranchRestDTOMapper.postpone(src.getTwinFactoryBranchKit(), mapperContext.forkOnPoint(FactoryBranchMode.Factory2FactoryBranchMode.SHORT));
        }
        if (showMultipliers) {
            dst.setMultiplierIdList(src.getTwinFactoryMultiplierKit().getIdSet());
            factoryMultiplierRestDTOMapper.postpone(src.getTwinFactoryMultiplierKit(), mapperContext.forkOnPoint(FactoryMultiplierMode.Factory2FactoryMultiplierMode.SHORT));
        }
        if (showConditionSets) {
            dst.setConditionSetIdList(src.getTwinFactoryConditionSetKit().getIdSet());
            factoryConditionSetRestDTOMapper.postpone(src.getTwinFactoryConditionSetKit(), mapperContext.forkOnPoint(FactoryConditionSetMode.TwinFactory2FactoryConditionSetMode.SHORT));
        }
        if (showErasers) {
            dst.setEraserIdList(src.getTwinFactoryEraserKit().getIdSet());
            factoryEraserRestDTOMapper.postpone(src.getTwinFactoryEraserKit(), mapperContext.forkOnPoint(FactoryEraserMode.Factory2FactoryEraserMode.SHORT));
        }
        if (showTriggers) {
            dst.setTriggerIdList(src.getTwinFactoryTriggerKit().getIdSet());
            factoryTriggerRestDTOMapper.postpone(src.getTwinFactoryTriggerKit(), mapperContext.forkOnPoint(FactoryTriggerMode.Factory2FactoryTriggerMode.SHORT));
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
            factoryService.countFactoryUsages(srcCollection);
        if (showFactoryPipelinesCount(mapperContext))
            factoryService.countFactoryPipelines(srcCollection);
        if (showFactoryMultipliersCount(mapperContext))
            factoryService.countFactoryMultipliers(srcCollection);
        if (showFactoryBranchesCount(mapperContext))
            factoryService.countFactoryBranches(srcCollection);
        if (showFactoryErasersCount(mapperContext))
            factoryService.countFactoryErasers(srcCollection);
        if (mapperContext.hasModeButNot(UserMode.Factory2UserMode.HIDE))
            factoryService.loadCreatedByUser(srcCollection);
        boolean showPipelines = mapperContext.hasModeButNot(FactoryPipelineMode.Factory2FactoryPipelineMode.HIDE);
        boolean showBranches = mapperContext.hasModeButNot(FactoryBranchMode.Factory2FactoryBranchMode.HIDE);
        boolean showMultipliers = mapperContext.hasModeButNot(FactoryMultiplierMode.Factory2FactoryMultiplierMode.HIDE);
        boolean showConditionSets = mapperContext.hasModeButNot(FactoryConditionSetMode.TwinFactory2FactoryConditionSetMode.HIDE);
        boolean showErasers = mapperContext.hasModeButNot(FactoryEraserMode.Factory2FactoryEraserMode.HIDE);
        boolean showTriggers = mapperContext.hasModeButNot(FactoryTriggerMode.Factory2FactoryTriggerMode.HIDE);
        if (showPipelines || showBranches || showMultipliers || showConditionSets || showErasers || showTriggers) {
            factoryService.loadFactoryElements(srcCollection);
        }
    }
}
