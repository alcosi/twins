package org.twins.core.mappers.rest.projection;

import org.springframework.stereotype.Component;
import org.twins.core.dao.projection.ProjectionTypeGroupEntity;
import org.twins.core.dto.rest.projection.ProjectionTypeGroupUpdateDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
public class ProjectionTypeGroupUpdateDTOReverseMapper extends RestSimpleDTOMapper<ProjectionTypeGroupUpdateDTOv1, ProjectionTypeGroupEntity> {

    @Override
    public void map(ProjectionTypeGroupUpdateDTOv1 src, ProjectionTypeGroupEntity dst, MapperContext mapperContext) throws Exception {
        dst
                .setId(src.getId())
                .setKey(src.getKey());
    }
}
