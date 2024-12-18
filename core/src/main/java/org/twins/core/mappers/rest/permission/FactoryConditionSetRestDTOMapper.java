package org.twins.core.mappers.rest.permission;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModeBinding;
import org.twins.core.dao.factory.TwinFactoryConditionSetEntity;
import org.twins.core.dto.rest.factory.FactoryConditionSetDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.MapperMode;
import org.twins.core.mappers.rest.mappercontext.modes.*;
import org.twins.core.service.factory.TwinFactoryService;

import java.util.Collection;

@Component
@RequiredArgsConstructor
@MapperModeBinding(modes = {FactoryConditionSetMode.class, ConditionSetInFactoryPipelineUsagesCountMode.class, ConditionSetInFactoryPipelineStepUsagesCountMode.class, ConditionSetInFactoryMultiplierFilterUsagesCount.class,
        ConditionSetInFactoryBranchUsagesCount.class, ConditionSetInFactoryEraserUsagesCountMode.class})
public class FactoryConditionSetRestDTOMapper extends RestSimpleDTOMapper<TwinFactoryConditionSetEntity, FactoryConditionSetDTOv1> {

    private final TwinFactoryService twinFactoryService;

    @Override
    public void map(TwinFactoryConditionSetEntity src, FactoryConditionSetDTOv1 dst, MapperContext mapperContext) throws Exception {
        switch (mapperContext.getModeOrUse(FactoryConditionSetMode.DETAILED)) {
            case DETAILED:
                dst
                        .setId(src.getId())
                        .setName(src.getName())
                        .setDescription(src.getDescription());
                break;
            case SHORT:
                dst
                        .setId(src.getId())
                        .setName(src.getName());
                break;
        }
        if (showFactoryObjectUsagesCount(mapperContext, ConditionSetInFactoryPipelineUsagesCountMode.HIDE)) {
            twinFactoryService.countConditionSetInFactoryPipelineUsages(src);
            dst.setId(src.getId()).setInFactoryPipelineUsagesCount(src.getInFactoryPipelineUsagesCount());
        }
        if (showFactoryObjectUsagesCount(mapperContext, ConditionSetInFactoryPipelineStepUsagesCountMode.HIDE)) {
            twinFactoryService.countConditionSetInFactoryPipelineStepUsages(src);
            dst.setId(src.getId()).setInFactoryPipelineStepUsagesCount(src.getInFactoryPipelineStepUsagesCount());
        }
        if (showFactoryObjectUsagesCount(mapperContext, ConditionSetInFactoryMultiplierFilterUsagesCount.HIDE)) {
            twinFactoryService.countConditionSetInFactoryMultiplierFilterUsages(src);
            dst.setId(src.getId()).setInFactoryMultiplierFilterUsagesCount(src.getInFactoryMultiplierFilterUsagesCount());
        }
        if (showFactoryObjectUsagesCount(mapperContext, ConditionSetInFactoryBranchUsagesCount.HIDE)) {
            twinFactoryService.countConditionSetInFactoryBranchUsages(src);
            dst.setId(src.getId()).setInFactoryBranchUsagesCount(src.getInFactoryBranchUsagesCount());
        }
        if (showFactoryObjectUsagesCount(mapperContext, ConditionSetInFactoryEraserUsagesCountMode.HIDE)) {
            twinFactoryService.countConditionSetInFactoryEraserUsages(src);
            dst.setId(src.getId()).setInFactoryEraserUsagesCount(src.getInFactoryEraserUsagesCount());
        }
    }

    public static boolean showFactoryObjectUsagesCount(MapperContext mapperContext, MapperMode mode) {
        return mapperContext.hasModeButNot(mode);
    }

    @Override
    public void beforeCollectionConversion(Collection<TwinFactoryConditionSetEntity> srcCollection, MapperContext mapperContext) throws Exception {
        super.beforeCollectionConversion(srcCollection, mapperContext);
        if (showFactoryObjectUsagesCount(mapperContext, ConditionSetInFactoryPipelineUsagesCountMode.HIDE))
            twinFactoryService.countConditionSetInFactoryPipelineUsages(srcCollection);
        if (showFactoryObjectUsagesCount(mapperContext, ConditionSetInFactoryPipelineStepUsagesCountMode.HIDE))
            twinFactoryService.countConditionSetInFactoryPipelineStepUsages(srcCollection);
        if (showFactoryObjectUsagesCount(mapperContext, ConditionSetInFactoryMultiplierFilterUsagesCount.HIDE))
            twinFactoryService.countConditionSetInFactoryMultiplierFilterUsages(srcCollection);
        if (showFactoryObjectUsagesCount(mapperContext, ConditionSetInFactoryBranchUsagesCount.HIDE))
            twinFactoryService.countConditionSetInFactoryBranchUsages(srcCollection);
        if (showFactoryObjectUsagesCount(mapperContext, ConditionSetInFactoryEraserUsagesCountMode.HIDE))
            twinFactoryService.countConditionSetInFactoryEraserUsages(srcCollection);
    }
}
