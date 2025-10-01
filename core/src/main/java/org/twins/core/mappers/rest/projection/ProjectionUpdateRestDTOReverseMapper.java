package org.twins.core.mappers.rest.projection;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.domain.projection.ProjectionUpdate;
import org.twins.core.dto.rest.projection.ProjectionUpdateDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
@RequiredArgsConstructor
public class ProjectionUpdateRestDTOReverseMapper extends RestSimpleDTOMapper<ProjectionUpdateDTOv1, ProjectionUpdate> {
    private final ProjectionSaveRestDTOReverseMapper projectionSaveRestDTOReverseMapper;

    @Override
    public void map(ProjectionUpdateDTOv1 src, ProjectionUpdate dst, MapperContext mapperContext) throws Exception {
        projectionSaveRestDTOReverseMapper.map(src, dst, mapperContext);
        dst
                .setId(src.getId());
    }
}
