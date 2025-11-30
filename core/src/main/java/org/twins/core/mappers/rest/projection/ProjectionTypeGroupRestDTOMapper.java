package org.twins.core.mappers.rest.projection;

import org.springframework.stereotype.Component;
import org.twins.core.dao.projection.ProjectionTypeGroupEntity;
import org.twins.core.dto.rest.projection.ProjectionTypeGroupDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
public class ProjectionTypeGroupRestDTOMapper extends RestSimpleDTOMapper<ProjectionTypeGroupEntity, ProjectionTypeGroupDTOv1> {

    @Override
    public void map(ProjectionTypeGroupEntity src, ProjectionTypeGroupDTOv1 dst, MapperContext mapperContext) throws Exception {
        dst
                .setId(src.getId())
                .setKey(src.getKey());
    }
}
