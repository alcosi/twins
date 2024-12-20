package org.twins.core.mappers.rest.factory;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModeBinding;
import org.twins.core.dao.factory.TwinFactoryPipelineEntity;
import org.twins.core.dao.permission.PermissionGrantSpaceRoleEntity;
import org.twins.core.domain.search.FactoryPipelineSearch;
import org.twins.core.dto.rest.factory.FactoryPipelineDTOv1;
import org.twins.core.dto.rest.factory.FactoryPipelineDTOv2;
import org.twins.core.dto.rest.permission.PermissionGrantSpaceRoleDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.FactoryPipelineMode;
import org.twins.core.mappers.rest.mappercontext.modes.PermissionGrantSpaceRoleMode;
import org.twins.core.service.factory.TwinFactoryService;

import java.util.Collection;

@Component
@RequiredArgsConstructor
@MapperModeBinding(modes = FactoryPipelineMode.class)
public class FactoryPipelineRestDTOMapper extends RestSimpleDTOMapper<TwinFactoryPipelineEntity, FactoryPipelineDTOv1> {

    private final TwinFactoryService twinFactoryService;

    @Override
    public void map(TwinFactoryPipelineEntity src, FactoryPipelineDTOv1 dst, MapperContext mapperContext) throws Exception {
        switch (mapperContext.getModeOrUse(PermissionGrantSpaceRoleMode.DETAILED)) {
            case DETAILED:
                twinFactoryService.countFactoryPipelineSteps(src);
                dst
                        .setId(src.getId())
                        .setFactoryId(src.getTwinFactoryId())
                        .setInputTwinClassId(src.getInputTwinClassId())
                        .setFactoryConditionSetId(src.getTwinFactoryConditionSetId())
                        .setFactoryConditionSetInvert(src.isTwinFactoryConditionInvert())
                        .setActive(src.isActive())
                        .setOutputTwinStatusId(src.getOutputTwinStatusId())
                        .setNextFactoryId(src.getNextTwinFactoryId())
                        .setNextFactoryLimitScope(src.isNextTwinFactoryLimitScope())
                        .setPipelineStepsCount(src.getPipelineStepsCount());
                break;
            case SHORT:
                dst
                        .setId(src.getId())
                        .setFactoryId(src.getTwinFactoryId())
                        .setInputTwinClassId(src.getInputTwinClassId());
                break;
        }
    }

    @Override
    public void beforeCollectionConversion(Collection<TwinFactoryPipelineEntity> srcCollection, MapperContext mapperContext) throws Exception {
        super.beforeCollectionConversion(srcCollection, mapperContext);
        if (mapperContext.hasMode(FactoryPipelineMode.DETAILED)) {
            twinFactoryService.countFactoryPipelineSteps(srcCollection);
        }
    }
}
