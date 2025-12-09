package org.twins.core.mappers.rest.projection;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.domain.search.ProjectionTypeSearch;
import org.twins.core.dto.rest.projection.ProjectionTypeSearchDTOv1;
import org.twins.core.mappers.rest.DataTimeRangeDTOReverseMapper;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
@RequiredArgsConstructor
public class ProjectionTypeSearchDTOReverseMapper extends RestSimpleDTOMapper<ProjectionTypeSearchDTOv1, ProjectionTypeSearch> {

    @Override
    public void map(ProjectionTypeSearchDTOv1 src, ProjectionTypeSearch dst, MapperContext mapperContext) throws Exception {
        dst
                .setIdList(src.getIdList())
                .setIdExcludeList(src.getIdExcludeList())
                .setKeyLikeList(src.getKeyLikeList())
                .setKeyNotLikeList(src.getKeyNotLikeList())
                .setNameLikeList(src.getNameLikeList())
                .setNameNotLikeList(src.getNameNotLikeList())
                .setProjectionTypeGroupIdList(src.getProjectionTypeGroupIdList())
                .setProjectionTypeGroupIdExcludeList(src.getProjectionTypeGroupIdExcludeList())
                .setMembershipTwinClassIdList(src.getMembershipTwinClassIdList())
                .setMembershipTwinClassIdExcludeList(src.getMembershipTwinClassIdExcludeList());
    }
}
