package org.twins.core.mappers.rest.projection;

import org.springframework.stereotype.Component;
import org.twins.core.dao.projection.ProjectionTypeEntity;
import org.twins.core.dto.rest.projection.ProjectionTypeCreateDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
public class ProjectionTypeCreateDTOReverseMapper extends RestSimpleDTOMapper<ProjectionTypeCreateDTOv1, ProjectionTypeEntity> {

    @Override
    public void map(ProjectionTypeCreateDTOv1 src, ProjectionTypeEntity dst, MapperContext mapperContext) throws Exception {
        dst
                .setKey(src.getKey())
                .setName(src.getName())
                .setProjectionTypeGroupId(src.getProjectionTypeGroupId())
                .setMembershipTwinClassId(src.getMembershipTwinClassId());
    }
}
