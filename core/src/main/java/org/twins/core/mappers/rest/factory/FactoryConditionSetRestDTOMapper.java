package org.twins.core.mappers.rest.factory;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModeBinding;
import org.twins.core.controller.rest.annotation.MapperModePointerBinding;
import org.twins.core.dao.factory.TwinFactoryConditionSetEntity;
import org.twins.core.dto.rest.factory.FactoryConditionSetDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.*;
import org.twins.core.mappers.rest.user.UserRestDTOMapper;
import org.twins.core.service.factory.FactoryConditionService;
import org.twins.core.service.factory.FactoryConditionSetService;
import org.twins.core.service.factory.FactoryService;

import java.util.Collection;

import static org.cambium.common.util.DateUtils.convertOrNull;

@Component
@RequiredArgsConstructor
@MapperModeBinding(modes = {
        FactoryConditionSetMode.class,
        ConditionSetInFactoryPipelineUsagesCountMode.class,
        ConditionSetInFactoryPipelineStepUsagesCountMode.class,
        ConditionSetInFactoryMultiplierFilterUsagesCountMode.class,
        ConditionSetInFactoryBranchUsagesCountMode.class,
        ConditionSetInFactoryEraserUsagesCountMode.class,
        ConditionSetInFactoryTriggerUsagesCountMode.class,})
public class FactoryConditionSetRestDTOMapper extends RestSimpleDTOMapper<TwinFactoryConditionSetEntity, FactoryConditionSetDTOv1> {

    @MapperModePointerBinding(modes = UserMode.FactoryConditionSet2UserMode.class)
    private final UserRestDTOMapper userRestDTOMapper;

    @MapperModePointerBinding(modes = FactoryMode.FactoryConditionSet2FactoryMode.class)
    private final FactoryRestDTOMapper factoryRestDTOMapper;

    private final FactoryService factoryService;

    private final FactoryConditionSetService factoryConditionSetService;

    private final FactoryConditionService factoryConditionService;

    @Lazy
    @Autowired
    @MapperModePointerBinding(modes = FactoryConditionMode.FactoryConditionSet2FactoryConditionMode.class)
    private FactoryConditionRestDTOMapper factoryConditionRestDTOMapper;

    @Override
    public void map(TwinFactoryConditionSetEntity src, FactoryConditionSetDTOv1 dst, MapperContext mapperContext) throws Exception {
        switch (mapperContext.getModeOrUse(FactoryConditionSetMode.DETAILED)) {
            case DETAILED ->
                dst
                        .setId(src.getId())
                        .setName(src.getName())
                        .setDescription(src.getDescription())
                        .setCreatedByUserId(src.getCreatedByUserId())
                        .setUpdatedAt(convertOrNull(src.getUpdatedAt()))
                        .setCreatedAt(convertOrNull(src.getCreatedAt()))
                        .setTwinFactoryId(src.getTwinFactoryId())
                        .setCachable(src.getCachable());
            case SHORT ->
                dst
                        .setId(src.getId())
                        .setName(src.getName())
                        .setTwinFactoryId(src.getTwinFactoryId())
                        .setCachable(src.getCachable());
        }
        if (mapperContext.hasModeButNot(ConditionSetInFactoryPipelineUsagesCountMode.HIDE)) {
            dst.setId(src.getId()).setUsageCountPipeline(src.getUsageCountPipeline());
        }
        if (mapperContext.hasModeButNot(ConditionSetInFactoryPipelineStepUsagesCountMode.HIDE)) {
            dst.setId(src.getId()).setUsageCountPipelineStep(src.getUsageCountPipelineStep());
        }
        if (mapperContext.hasModeButNot(ConditionSetInFactoryMultiplierFilterUsagesCountMode.HIDE)) {
            dst.setId(src.getId()).setUsageCountMultiplierFilter(src.getUsageCountMultiplierFilter());
        }
        if (mapperContext.hasModeButNot(ConditionSetInFactoryBranchUsagesCountMode.HIDE)) {
            dst.setId(src.getId()).setUsageCountBranch(src.getUsageCountBranch());
        }
        if (mapperContext.hasModeButNot(ConditionSetInFactoryEraserUsagesCountMode.HIDE)) {
            dst.setId(src.getId()).setUsageCountEraser(src.getUsageCountEraser());
        }
        if (mapperContext.hasModeButNot(ConditionSetInFactoryTriggerUsagesCountMode.HIDE)) {
            dst.setId(src.getId()).setUsageCountTrigger(src.getUsageCountTrigger());
        }
        if (mapperContext.hasModeButNot(UserMode.FactoryConditionSet2UserMode.HIDE)) {
            dst.setCreatedByUserId(src.getCreatedByUserId());
            factoryConditionSetService.loadCreatedByUser(src);
            userRestDTOMapper.postpone(src.getCreatedByUser(), mapperContext.forkOnPoint(UserMode.FactoryConditionSet2UserMode.HIDE));
        }
        if (mapperContext.hasModeButNot(FactoryMode.FactoryConditionSet2FactoryMode.HIDE)) {
            factoryConditionSetService.loadFactory(src);
            dst.setTwinFactoryId(src.getTwinFactoryId());
            factoryRestDTOMapper.postpone(src.getTwinFactory(), mapperContext.forkOnPoint(FactoryMode.FactoryConditionSet2FactoryMode.SHORT));
        }
        if (mapperContext.hasModeButNot(FactoryConditionMode.FactoryConditionSet2FactoryConditionMode.HIDE)) {
            factoryConditionService.loadConditions(src);
            dst.setConditionIdList(src.getTwinFactoryConditionKit().getIdSet());
            factoryConditionRestDTOMapper.postpone(src.getTwinFactoryConditionKit(), mapperContext.forkOnPoint(FactoryConditionMode.FactoryConditionSet2FactoryConditionMode.SHORT));
        }
    }

    @Override
    public void beforeCollectionConversion(Collection<TwinFactoryConditionSetEntity> srcCollection, MapperContext mapperContext) throws Exception {
        super.beforeCollectionConversion(srcCollection, mapperContext);
        if (mapperContext.hasModeButNot(FactoryMode.FactoryConditionSet2FactoryMode.HIDE))
            factoryConditionSetService.loadFactory(srcCollection);
        if (mapperContext.hasModeButNot(UserMode.FactoryConditionSet2UserMode.HIDE))
            factoryConditionSetService.loadCreatedByUser(srcCollection);
        if (mapperContext.hasModeButNot(FactoryConditionMode.FactoryConditionSet2FactoryConditionMode.HIDE))
            factoryConditionService.loadConditions(srcCollection);
    }
}
