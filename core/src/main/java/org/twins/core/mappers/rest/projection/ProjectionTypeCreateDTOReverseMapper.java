package org.twins.core.mappers.rest.projection;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.dao.projection.ProjectionTypeEntity;
import org.twins.core.dto.rest.projection.ProjectionTypeCreateDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
@RequiredArgsConstructor
public class ProjectionTypeCreateDTOReverseMapper extends RestSimpleDTOMapper<ProjectionTypeCreateDTOv1, ProjectionTypeEntity> {
    private final ProjectionTypeSaveDTOReverseMapper projectionTypeSaveDTOReverseMapper;

    @Override
    public void map(ProjectionTypeCreateDTOv1 src, ProjectionTypeEntity dst, MapperContext mapperContext) throws Exception {
        projectionTypeSaveDTOReverseMapper.map(src, dst, mapperContext);
    }
}
