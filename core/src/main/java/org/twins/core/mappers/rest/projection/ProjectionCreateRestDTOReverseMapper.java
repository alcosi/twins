package org.twins.core.mappers.rest.projection;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.domain.projection.ProjectionCreate;
import org.twins.core.dto.rest.projection.ProjectionCreateDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
@RequiredArgsConstructor
public class ProjectionCreateRestDTOReverseMapper extends RestSimpleDTOMapper<ProjectionCreateDTOv1, ProjectionCreate> {
    private final ProjectionSaveRestDTOReverseMapper projectionSaveRestDTOReverseMapper;

    @Override
    public void map(ProjectionCreateDTOv1 src, ProjectionCreate dst, MapperContext mapperContext) throws Exception {
        projectionSaveRestDTOReverseMapper.map(src, dst, mapperContext);
    }
}
