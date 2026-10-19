package org.twins.core.mappers.rest.projection;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.domain.search.ProjectionTypeGroupSearch;
import org.twins.core.dto.rest.projection.ProjectionTypeGroupSearchDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
@RequiredArgsConstructor
public class ProjectionTypeGroupSearchDTOReverseMapper extends RestSimpleDTOMapper<ProjectionTypeGroupSearchDTOv1, ProjectionTypeGroupSearch> {

    @Override
    public void map(ProjectionTypeGroupSearchDTOv1 src, ProjectionTypeGroupSearch dst, MapperContext mapperContext) throws Exception {
        dst
                .setIdList(src.getIdList())
                .setIdExcludeList(src.getIdExcludeList())
                .setKeyLikeList(src.getKeyLikeList())
                .setKeyNotLikeList(src.getKeyNotLikeList());
    }
}
