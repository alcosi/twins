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
        ConditionSetInFactoryEraserUsagesCountMode.class,})
public class FactoryConditionSetRestDTOMapper extends RestSimpleDTOMapper<TwinFactoryConditionSetEntity, FactoryConditionSetDTOv1> {

    @MapperModePointerBinding(modes = UserMode.FactoryConditionSet2UserMode.class)
    private final UserRestDTOMapper userRestDTOMapper;

    @MapperModePointerBinding(modes = FactoryMode.FactoryConditionSet2FactoryMode.class)
    private final FactoryRestDTOMapper factoryRestDTOMapper;

    private final FactoryService factoryService;

    private final FactoryConditionSetService factoryConditionSetService;

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
            factoryService.countConditionSetInFactoryPipelineUsages(src);
            dst.setId(src.getId()).setInFactoryPipelineUsagesCount(src.getInFactoryPipelineUsagesCount());
        }
        if (mapperContext.hasModeButNot(ConditionSetInFactoryPipelineStepUsagesCountMode.HIDE)) {
            factoryService.countConditionSetInFactoryPipelineStepUsages(src);
            dst.setId(src.getId()).setInFactoryPipelineStepUsagesCount(src.getInFactoryPipelineStepUsagesCount());
        }
        if (mapperContext.hasModeButNot(ConditionSetInFactoryMultiplierFilterUsagesCountMode.HIDE)) {
            factoryService.countConditionSetInFactoryMultiplierFilterUsages(src);
            dst.setId(src.getId()).setInFactoryMultiplierFilterUsagesCount(src.getInFactoryMultiplierFilterUsagesCount());
        }
        if (mapperContext.hasModeButNot(ConditionSetInFactoryBranchUsagesCountMode.HIDE)) {
            factoryService.countConditionSetInFactoryBranchUsages(src);
            dst.setId(src.getId()).setInFactoryBranchUsagesCount(src.getInFactoryBranchUsagesCount());
        }
        if (mapperContext.hasModeButNot(ConditionSetInFactoryEraserUsagesCountMode.HIDE)) {
            factoryService.countConditionSetInFactoryEraserUsages(src);
            dst.setId(src.getId()).setInFactoryEraserUsagesCount(src.getInFactoryEraserUsagesCount());
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
    }

    @Override
    public void beforeCollectionConversion(Collection<TwinFactoryConditionSetEntity> srcCollection, MapperContext mapperContext) throws Exception {
        super.beforeCollectionConversion(srcCollection, mapperContext);
        if (mapperContext.hasModeButNot(ConditionSetInFactoryPipelineUsagesCountMode.HIDE))
            factoryService.countConditionSetInFactoryPipelineUsages(srcCollection);
        if (mapperContext.hasModeButNot(ConditionSetInFactoryPipelineStepUsagesCountMode.HIDE))
            factoryService.countConditionSetInFactoryPipelineStepUsages(srcCollection);
        if (mapperContext.hasModeButNot(ConditionSetInFactoryMultiplierFilterUsagesCountMode.HIDE))
            factoryService.countConditionSetInFactoryMultiplierFilterUsages(srcCollection);
        if (mapperContext.hasModeButNot(ConditionSetInFactoryBranchUsagesCountMode.HIDE))
            factoryService.countConditionSetInFactoryBranchUsages(srcCollection);
        if (mapperContext.hasModeButNot(ConditionSetInFactoryEraserUsagesCountMode.HIDE))
            factoryService.countConditionSetInFactoryEraserUsages(srcCollection);
        if (mapperContext.hasModeButNot(FactoryMode.FactoryConditionSet2FactoryMode.HIDE))
            factoryConditionSetService.loadFactory(srcCollection);
        if (mapperContext.hasModeButNot(UserMode.FactoryConditionSet2UserMode.HIDE))
            factoryConditionSetService.loadCreatedByUser(srcCollection);
    }
}
