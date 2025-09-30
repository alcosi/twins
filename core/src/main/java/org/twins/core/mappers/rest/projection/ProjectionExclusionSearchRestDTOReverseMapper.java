package org.twins.core.mappers.rest.projection;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.dto.rest.projection.ProjectionExclusionSearch;
import org.twins.core.dto.rest.projection.ProjectionExclusionSearchDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
@RequiredArgsConstructor
public class ProjectionExclusionSearchRestDTOReverseMapper extends RestSimpleDTOMapper<ProjectionExclusionSearchDTOv1, ProjectionExclusionSearch> {

    @Override
    public void map(ProjectionExclusionSearchDTOv1 src, ProjectionExclusionSearch dst, MapperContext mapperContext) throws Exception {
        dst
                .setIdList(src.getIdList())
                .setIdExcludeList(src.getIdExcludeList())
                .setTwinIdList(src.getTwinIdList())
                .setTwinIdExcludeList(src.getTwinIdExcludeList())
                .setTwinClassFieldIdList(src.getTwinClassFieldIdList())
                .setTwinClassFieldIdExcludeList(src.getTwinClassFieldIdExcludeList());
    }
}
