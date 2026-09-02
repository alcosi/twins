package org.twins.core.mappers.rest.projection;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.dao.projection.ProjectionTypeGroupEntity;
import org.twins.core.dto.rest.projection.ProjectionTypeGroupUpdateDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
@RequiredArgsConstructor
public class ProjectionTypeGroupUpdateDTOReverseMapper extends RestSimpleDTOMapper<ProjectionTypeGroupUpdateDTOv1, ProjectionTypeGroupEntity> {
    private final ProjectionTypeGroupSaveDTOReverseMapper projectionTypeGroupSaveDTOReverseMapper;

    @Override
    public void map(ProjectionTypeGroupUpdateDTOv1 src, ProjectionTypeGroupEntity dst, MapperContext mapperContext) throws Exception {
        dst.setId(src.getId());
        projectionTypeGroupSaveDTOReverseMapper.map(src, dst, mapperContext);
    }
}
