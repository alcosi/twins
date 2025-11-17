package org.twins.core.mappers.rest.projection;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModeBinding;
import org.twins.core.dao.projection.ProjectionEntity;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dto.rest.projection.ProjectionDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.ProjectionMode;
import org.twins.core.service.face.FaceTwinPointerService;

@Component
@RequiredArgsConstructor
@MapperModeBinding(modes = ProjectionMode.class)
public class ProjectionRestDTOMapper extends RestSimpleDTOMapper<ProjectionEntity, ProjectionDTOv1> {
    private final FaceTwinPointerService faceTwinPointerService;

    @Override
    public void map(ProjectionEntity src, ProjectionDTOv1 dst, MapperContext mapperContext) throws Exception {
        TwinEntity srcTwin = faceTwinPointerService.getPointer(src.getSrcTwinPointerId());
        switch (mapperContext.getModeOrUse(ProjectionMode.DETAILED)) {
            case DETAILED -> dst
                    .setId(src.getId())
                    .setSrcPointedTwinId(srcTwin == null ? null : srcTwin.getId())
                    .setSrcTwinClassFieldId(src.getSrcTwinClassFieldId())
                    .setDstTwinClassId(src.getDstTwinClassId())
                    .setDstTwinClassFieldId(src.getDstTwinClassFieldId());

            case SHORT -> dst
                    .setId(src.getId())
                    .setSrcTwinClassFieldId(src.getSrcTwinClassFieldId())
                    .setDstTwinClassFieldId(src.getDstTwinClassFieldId());
        }
    }
}
