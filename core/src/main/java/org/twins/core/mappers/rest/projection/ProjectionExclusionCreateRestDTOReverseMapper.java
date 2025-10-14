package org.twins.core.mappers.rest.projection;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.domain.projection.ProjectionExclusionCreate;
import org.twins.core.dto.rest.projection.ProjectionExclusionCreateDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
@RequiredArgsConstructor
public class ProjectionExclusionCreateRestDTOReverseMapper extends RestSimpleDTOMapper<ProjectionExclusionCreateDTOv1, ProjectionExclusionCreate> {
    private final ProjectionExclusionSaveRestDTOReverseMapper projectionExclusionSaveRestDTOReverseMapper;


    @Override
    public void map(ProjectionExclusionCreateDTOv1 src, ProjectionExclusionCreate dst, MapperContext mapperContext) throws Exception {
        projectionExclusionSaveRestDTOReverseMapper.map(src, dst, mapperContext);
    }
}
