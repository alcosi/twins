package org.twins.core.mappers.rest.projection;

import org.springframework.stereotype.Component;
import org.twins.core.dao.projection.ProjectionTypeGroupEntity;
import org.twins.core.dto.rest.projection.ProjectionTypeGroupCreateDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
public class ProjectionTypeGroupCreateDTOReverseMapper extends RestSimpleDTOMapper<ProjectionTypeGroupCreateDTOv1, ProjectionTypeGroupEntity> {

    @Override
    public void map(ProjectionTypeGroupCreateDTOv1 src, ProjectionTypeGroupEntity dst, MapperContext mapperContext) throws Exception {
        dst
                .setKey(src.getKey());
    }
}
