package org.twins.core.mappers.rest.projection;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.domain.search.ProjectionSearch;
import org.twins.core.dto.rest.projection.ProjectionSearchDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
@RequiredArgsConstructor
public class ProjectionSearchRestDTOReverseMapper extends RestSimpleDTOMapper<ProjectionSearchDTOv1, ProjectionSearch> {

    @Override
    public void map(ProjectionSearchDTOv1 src, ProjectionSearch dst, MapperContext mapperContext) throws Exception {
        dst
                .setIdList(src.getIdList())
                .setIdExcludeList(src.getIdExcludeList())
                .setSrcTwinPointerIdList(src.getSrcTwinPointerIdList())
                .setSrcTwinPointerIdExcludeList(src.getSrcTwinPointerIdExcludeList())
                .setSrcTwinClassFieldIdList(src.getSrcTwinClassFieldIdList())
                .setSrcTwinClassFieldIdExcludeList(src.getSrcTwinClassFieldIdExcludeList())
                .setDstTwinClassIdList(src.getDstTwinClassIdList())
                .setDstTwinClassIdExcludeList(src.getDstTwinClassIdExcludeList())
                .setDstTwinClassFieldIdList(src.getDstTwinClassFieldIdList())
                .setDstTwinClassFieldIdExcludeList(src.getDstTwinClassFieldIdExcludeList())
                .setProjectionTypeIdList(src.getProjectionTypeIdList())
                .setProjectionTypeIdExcludeList(src.getProjectionTypeIdExcludeList())
                .setFieldProjectorIdList(src.getFieldProjectorIdList())
                .setFieldProjectorIdExcludeList(src.getFieldProjectorIdExcludeList())
                .setActive(src.getActive());
    }
}
