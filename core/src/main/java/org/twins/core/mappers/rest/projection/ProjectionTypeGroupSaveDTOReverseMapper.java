package org.twins.core.mappers.rest.projection;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.dao.projection.ProjectionTypeGroupEntity;
import org.twins.core.dto.rest.projection.ProjectionTypeGroupSaveDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
@RequiredArgsConstructor
public class ProjectionTypeGroupSaveDTOReverseMapper extends RestSimpleDTOMapper<ProjectionTypeGroupSaveDTOv1, ProjectionTypeGroupEntity> {
    @Override
    public void map(ProjectionTypeGroupSaveDTOv1 src, ProjectionTypeGroupEntity dst, MapperContext mapperContext) throws Exception {
        dst
                .setKey(src.getKey());
    }
}
