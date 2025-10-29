package org.twins.core.mappers.rest.projection;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.dao.projection.ProjectionExclusionEntity;
import org.twins.core.dto.rest.projection.ProjectionExclusionDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
@RequiredArgsConstructor
public class ProjectionExclusionRestDTOMapper extends RestSimpleDTOMapper<ProjectionExclusionEntity, ProjectionExclusionDTOv1> {

    @Override
    public void map(ProjectionExclusionEntity src, ProjectionExclusionDTOv1 dst, MapperContext mapperContext) throws Exception {
        dst
                .setId(src.getId())
                .setTwinId(src.getTwinId())
                .setTwinClassFieldId(src.getTwinClassFieldId());
    }
}
