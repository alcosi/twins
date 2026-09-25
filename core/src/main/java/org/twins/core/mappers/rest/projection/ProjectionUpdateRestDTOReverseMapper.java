package org.twins.core.mappers.rest.projection;

import org.springframework.stereotype.Component;
import org.twins.core.domain.projection.ProjectionUpdate;
import org.twins.core.dto.rest.projection.ProjectionUpdateDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
public class ProjectionUpdateRestDTOReverseMapper extends RestSimpleDTOMapper<ProjectionUpdateDTOv1, ProjectionUpdate> {

    @Override
    public void map(ProjectionUpdateDTOv1 src, ProjectionUpdate dst, MapperContext mapperContext) throws Exception {
        dst.setId(src.getId());
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
