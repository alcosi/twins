package org.twins.core.mappers.rest.projection;

import org.springframework.stereotype.Component;
import org.twins.core.dao.projection.ProjectionTypeEntity;
import org.twins.core.dto.rest.projection.ProjectionTypeUpdateDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
public class ProjectionTypeUpdateDTOReverseMapper extends RestSimpleDTOMapper<ProjectionTypeUpdateDTOv1, ProjectionTypeEntity> {

    @Override
    public void map(ProjectionTypeUpdateDTOv1 src, ProjectionTypeEntity dst, MapperContext mapperContext) throws Exception {
        dst
                .setKey(src.getKey())
                .setName(src.getName())
                .setProjectionTypeGroupId(src.getProjectionTypeGroupId())
                .setMembershipTwinClassId(src.getMembershipTwinClassId())
                .setId(src.getId());
    }
}
