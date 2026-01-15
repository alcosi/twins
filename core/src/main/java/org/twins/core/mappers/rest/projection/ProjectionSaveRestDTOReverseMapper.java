package org.twins.core.mappers.rest.projection;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.domain.projection.ProjectionSave;
import org.twins.core.dto.rest.projection.ProjectionSaveDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
@RequiredArgsConstructor
public class ProjectionSaveRestDTOReverseMapper extends RestSimpleDTOMapper<ProjectionSaveDTOv1, ProjectionSave> {

    @Override
    public void map(ProjectionSaveDTOv1 src, ProjectionSave dst, MapperContext mapperContext) throws Exception {
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
