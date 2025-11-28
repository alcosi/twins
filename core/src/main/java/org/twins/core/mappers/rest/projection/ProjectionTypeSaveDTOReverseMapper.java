package org.twins.core.mappers.rest.projection;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.dao.projection.ProjectionTypeEntity;
import org.twins.core.dto.rest.projection.ProjectionTypeSaveDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
@RequiredArgsConstructor
public class ProjectionTypeSaveDTOReverseMapper extends RestSimpleDTOMapper<ProjectionTypeSaveDTOv1, ProjectionTypeEntity> {
    @Override
    public void map(ProjectionTypeSaveDTOv1 src, ProjectionTypeEntity dst, MapperContext mapperContext) throws Exception {
        dst
                .setKey(src.getKey())
                .setName(src.getName())
                .setProjectionTypeGroupId(src.getProjectionTypeGroupId())
                .setMembershipTwinClassId(src.getMembershipTwinClassId());
    }
}
