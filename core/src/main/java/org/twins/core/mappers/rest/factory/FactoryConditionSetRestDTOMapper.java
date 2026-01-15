package org.twins.core.mappers.rest.factory;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModeBinding;
import org.twins.core.controller.rest.annotation.MapperModePointerBinding;
import org.twins.core.dao.factory.TwinFactoryConditionSetEntity;
import org.twins.core.dto.rest.factory.FactoryConditionSetDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.*;
import org.twins.core.mappers.rest.user.UserRestDTOMapper;
import org.twins.core.service.factory.TwinFactoryService;

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
        ConditionSetInFactoryEraserUsagesCountMode.class,})
public class FactoryConditionSetRestDTOMapper extends RestSimpleDTOMapper<TwinFactoryConditionSetEntity, FactoryConditionSetDTOv1> {

    @MapperModePointerBinding(modes = UserMode.FactoryConditionSet2UserMode.class)
    private final UserRestDTOMapper userRestDTOMapper;

    private final TwinFactoryService twinFactoryService;

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
                        .setTwinFactoryId(src.getTwinFactoryId());
            case SHORT ->
                dst
                        .setId(src.getId())
                        .setName(src.getName())
                        .setTwinFactoryId(src.getTwinFactoryId());
        }
        if (mapperContext.hasModeButNot(ConditionSetInFactoryPipelineUsagesCountMode.HIDE)) {
            twinFactoryService.countConditionSetInFactoryPipelineUsages(src);
            dst.setId(src.getId()).setInFactoryPipelineUsagesCount(src.getInFactoryPipelineUsagesCount());
        }
        if (mapperContext.hasModeButNot(ConditionSetInFactoryPipelineStepUsagesCountMode.HIDE)) {
            twinFactoryService.countConditionSetInFactoryPipelineStepUsages(src);
            dst.setId(src.getId()).setInFactoryPipelineStepUsagesCount(src.getInFactoryPipelineStepUsagesCount());
        }
        if (mapperContext.hasModeButNot(ConditionSetInFactoryMultiplierFilterUsagesCountMode.HIDE)) {
            twinFactoryService.countConditionSetInFactoryMultiplierFilterUsages(src);
            dst.setId(src.getId()).setInFactoryMultiplierFilterUsagesCount(src.getInFactoryMultiplierFilterUsagesCount());
        }
        if (mapperContext.hasModeButNot(ConditionSetInFactoryBranchUsagesCountMode.HIDE)) {
            twinFactoryService.countConditionSetInFactoryBranchUsages(src);
            dst.setId(src.getId()).setInFactoryBranchUsagesCount(src.getInFactoryBranchUsagesCount());
        }
        if (mapperContext.hasModeButNot(ConditionSetInFactoryEraserUsagesCountMode.HIDE)) {
            twinFactoryService.countConditionSetInFactoryEraserUsages(src);
            dst.setId(src.getId()).setInFactoryEraserUsagesCount(src.getInFactoryEraserUsagesCount());
        }
        if (mapperContext.hasModeButNot(UserMode.FactoryConditionSet2UserMode.HIDE)) {
            dst.setCreatedByUserId(src.getCreatedByUserId());
            userRestDTOMapper.postpone(src.getCreatedByUser(), mapperContext.forkOnPoint(UserMode.FactoryConditionSet2UserMode.HIDE));
        }
    }

    @Override
    public void beforeCollectionConversion(Collection<TwinFactoryConditionSetEntity> srcCollection, MapperContext mapperContext) throws Exception {
        super.beforeCollectionConversion(srcCollection, mapperContext);
        if (mapperContext.hasModeButNot(ConditionSetInFactoryPipelineUsagesCountMode.HIDE))
            twinFactoryService.countConditionSetInFactoryPipelineUsages(srcCollection);
        if (mapperContext.hasModeButNot(ConditionSetInFactoryPipelineStepUsagesCountMode.HIDE))
            twinFactoryService.countConditionSetInFactoryPipelineStepUsages(srcCollection);
        if (mapperContext.hasModeButNot(ConditionSetInFactoryMultiplierFilterUsagesCountMode.HIDE))
            twinFactoryService.countConditionSetInFactoryMultiplierFilterUsages(srcCollection);
        if (mapperContext.hasModeButNot(ConditionSetInFactoryBranchUsagesCountMode.HIDE))
            twinFactoryService.countConditionSetInFactoryBranchUsages(srcCollection);
        if (mapperContext.hasModeButNot(ConditionSetInFactoryEraserUsagesCountMode.HIDE))
            twinFactoryService.countConditionSetInFactoryEraserUsages(srcCollection);
    }
}
