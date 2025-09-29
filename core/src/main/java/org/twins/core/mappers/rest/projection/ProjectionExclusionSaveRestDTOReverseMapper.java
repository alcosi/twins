package org.twins.core.mappers.rest.projection;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.domain.projection.ProjectionExclusionSave;
import org.twins.core.dto.rest.projection.ProjectionExclusionSaveDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
@RequiredArgsConstructor
public class ProjectionExclusionSaveRestDTOReverseMapper extends RestSimpleDTOMapper<ProjectionExclusionSaveDTOv1, ProjectionExclusionSave> {

    @Override
    public void map(ProjectionExclusionSaveDTOv1 src, ProjectionExclusionSave dst, MapperContext mapperContext) throws Exception {
        dst
                .setTwinId(src.getTwinId())
                .setTwinClassFieldId(src.getTwinClassFieldId());
    }
}
