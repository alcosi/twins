package org.twins.core.mappers.rest.projection;

import org.springframework.stereotype.Component;
import org.twins.core.domain.projection.ProjectionCreate;
import org.twins.core.dto.rest.projection.ProjectionCreateDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
public class ProjectionCreateRestDTOReverseMapper extends RestSimpleDTOMapper<ProjectionCreateDTOv1, ProjectionCreate> {

    @Override
    public void map(ProjectionCreateDTOv1 src, ProjectionCreate dst, MapperContext mapperContext) throws Exception {
        dst
                .setSrcTwinPointerId(src.getSrcTwinPointerId())
                .setSrcTwinClassFieldId(src.getSrcTwinClassFieldId())
                .setDstTwinClassId(src.getDstTwinClassId())
                .setDstTwinClassFieldId(src.getDstTwinClassFieldId())
                .setFieldProjectorFeaturerId(src.getFieldProjectorFeaturerId())
                .setFieldProjectorParams(src.getFieldProjectorParams())
                .setProjectionTypeId(src.getProjectionTypeId())
                .setActive(src.getActive());
    }
}
