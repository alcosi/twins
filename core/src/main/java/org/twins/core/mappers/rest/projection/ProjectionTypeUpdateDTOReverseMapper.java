package org.twins.core.mappers.rest.projection;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.dao.projection.ProjectionTypeEntity;
import org.twins.core.dto.rest.projection.ProjectionTypeUpdateDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
@RequiredArgsConstructor
public class ProjectionTypeUpdateDTOReverseMapper extends RestSimpleDTOMapper<ProjectionTypeUpdateDTOv1, ProjectionTypeEntity> {
    private final ProjectionTypeSaveDTOReverseMapper projectionTypeSaveDTOReverseMapper;

    @Override
    public void map(ProjectionTypeUpdateDTOv1 src, ProjectionTypeEntity dst, MapperContext mapperContext) throws Exception {
        projectionTypeSaveDTOReverseMapper.map(src, dst, mapperContext);
        dst.setId(src.getId());
    }
}
