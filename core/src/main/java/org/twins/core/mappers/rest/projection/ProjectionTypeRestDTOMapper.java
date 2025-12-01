package org.twins.core.mappers.rest.projection;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModeBinding;
import org.twins.core.controller.rest.annotation.MapperModePointerBinding;
import org.twins.core.dao.projection.ProjectionTypeEntity;
import org.twins.core.dto.rest.projection.ProjectionTypeDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.ProjectionTypeGroupMode;
import org.twins.core.mappers.rest.mappercontext.modes.ProjectionTypeMode;
import org.twins.core.mappers.rest.mappercontext.modes.TwinClassMode;
import org.twins.core.mappers.rest.twinclass.TwinClassRestDTOMapper;

@Component
@RequiredArgsConstructor
@MapperModeBinding(modes = ProjectionTypeMode.class)
public class ProjectionTypeRestDTOMapper extends RestSimpleDTOMapper<ProjectionTypeEntity, ProjectionTypeDTOv1> {

    @MapperModePointerBinding(modes = TwinClassMode.ProjectionType2TwinClassMode.class)
    private final TwinClassRestDTOMapper twinClassRestDTOMapper;

    @MapperModePointerBinding(modes = ProjectionTypeGroupMode.ProjectionType2ProjectionTypeGroupMode.class)
    private final ProjectionTypeGroupRestDTOMapper projectionTypeGroupRestDTOMapper;

    @Override
    public void map(ProjectionTypeEntity src, ProjectionTypeDTOv1 dst, MapperContext mapperContext) throws Exception {
        switch (mapperContext.getModeOrUse(ProjectionTypeMode.DETAILED)) {
            case DETAILED ->
                dst
                        .setId(src.getId())
                        .setName(src.getName())
                        .setKey(src.getKey())
                        .setMembershipTwinClassId(src.getMembershipTwinClassId())
                        .setProjectionTypeGroupId(src.getProjectionTypeGroupId());
            case SHORT ->
                dst
                        .setId(src.getId())
                        .setName(src.getName())
                        .setKey(src.getKey());
        }

        if (mapperContext.hasModeButNot(TwinClassMode.ProjectionType2TwinClassMode.HIDE)) {
            dst.setMembershipTwinClassId(src.getMembershipTwinClassId());
            twinClassRestDTOMapper.postpone(src.getMembershipTwinClass(), mapperContext.forkOnPoint(mapperContext.getModeOrUse(TwinClassMode.ProjectionType2TwinClassMode.SHORT)));
        }

        if (mapperContext.hasMode(ProjectionTypeGroupMode.ProjectionType2ProjectionTypeGroupMode.SHOW)) {
            dst.setProjectionTypeGroupId(src.getProjectionTypeGroupId());
            projectionTypeGroupRestDTOMapper.postpone(src.getProjectionTypeGroup(), mapperContext.forkOnPoint(mapperContext.getModeOrUse(ProjectionTypeGroupMode.ProjectionType2ProjectionTypeGroupMode.SHOW)));
        }
    }
}
