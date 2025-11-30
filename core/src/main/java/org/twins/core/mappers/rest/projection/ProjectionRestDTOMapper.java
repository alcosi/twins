package org.twins.core.mappers.rest.projection;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModeBinding;
import org.twins.core.controller.rest.annotation.MapperModePointerBinding;
import org.twins.core.dao.projection.ProjectionEntity;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dto.rest.projection.ProjectionDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.ProjectionMode;
import org.twins.core.mappers.rest.mappercontext.modes.ProjectionTypeMode;
import org.twins.core.mappers.rest.mappercontext.modes.TwinClassFieldMode;
import org.twins.core.mappers.rest.mappercontext.modes.TwinClassMode;
import org.twins.core.mappers.rest.twinclass.TwinClassFieldRestDTOMapper;
import org.twins.core.mappers.rest.twinclass.TwinClassRestDTOMapper;
import org.twins.core.service.face.FaceTwinPointerService;

@Component
@RequiredArgsConstructor
@MapperModeBinding(modes = ProjectionMode.class)
public class ProjectionRestDTOMapper extends RestSimpleDTOMapper<ProjectionEntity, ProjectionDTOv1> {
    private final FaceTwinPointerService faceTwinPointerService;

    @MapperModePointerBinding(modes = TwinClassFieldMode.Projection2TwinClassFieldMode.class)
    private final TwinClassFieldRestDTOMapper twinClassFieldRestDTOMapper;

    @MapperModePointerBinding(modes = TwinClassMode.Projection2TwinClassMode.class)
    private final TwinClassRestDTOMapper twinClassRestDTOMapper;

    @MapperModePointerBinding(modes = ProjectionTypeMode.Projection2ProjectionTypeMode.class)
    private final ProjectionTypeRestDTOMapper projectionTypeRestDTOMapper;

    @Override
    public void map(ProjectionEntity src, ProjectionDTOv1 dst, MapperContext mapperContext) throws Exception {
        TwinEntity srcTwin = faceTwinPointerService.getPointer(src.getSrcTwinPointerId());
        switch (mapperContext.getModeOrUse(ProjectionMode.MANAGED)) {
            case MANAGED -> dst
                    .setId(src.getId())
                    .setSrcPointedTwinId(srcTwin == null ? null : srcTwin.getId())
                    .setSrcTwinClassFieldId(src.getSrcTwinClassFieldId())
                    .setDstTwinClassId(src.getDstTwinClassId())
                    .setDstTwinClassFieldId(src.getDstTwinClassFieldId())
                    .setFieldProjectorFeaturerId(src.getFieldProjectorFeaturerId())
                    .setFieldProjectorParams(src.getFieldProjectorParams())
                    .setProjectionTypeId(src.getProjectionTypeId());

            case DETAILED -> dst
                    .setId(src.getId())
                    .setSrcPointedTwinId(srcTwin == null ? null : srcTwin.getId())
                    .setSrcTwinClassFieldId(src.getSrcTwinClassFieldId())
                    .setDstTwinClassId(src.getDstTwinClassId())
                    .setDstTwinClassFieldId(src.getDstTwinClassFieldId())
                    .setProjectionTypeId(src.getProjectionTypeId());

            case SHORT -> dst
                    .setId(src.getId())
                    .setSrcTwinClassFieldId(src.getSrcTwinClassFieldId())
                    .setDstTwinClassFieldId(src.getDstTwinClassFieldId());
        }

        if (mapperContext.hasMode(ProjectionTypeMode.Projection2ProjectionTypeMode.SHOW)) {
            dst.setProjectionTypeId(src.getProjectionTypeId());
            projectionTypeRestDTOMapper.postpone(src.getProjectionType(), mapperContext.forkOnPoint(mapperContext.getModeOrUse(ProjectionTypeMode.Projection2ProjectionTypeMode.SHOW)));
        }

        if (mapperContext.hasModeButNot(TwinClassFieldMode.Projection2TwinClassFieldMode.HIDE)) {
            dst
                    .setSrcTwinClassFieldId(src.getSrcTwinClassFieldId())
                    .setDstTwinClassFieldId(src.getDstTwinClassFieldId());

            twinClassFieldRestDTOMapper.postpone(src.getSrcTwinClassField(), mapperContext.forkOnPoint(mapperContext.getModeOrUse(TwinClassFieldMode.Projection2TwinClassFieldMode.SHORT)));
            twinClassFieldRestDTOMapper.postpone(src.getDstTwinClassField(), mapperContext.forkOnPoint(mapperContext.getModeOrUse(TwinClassFieldMode.Projection2TwinClassFieldMode.SHORT)));
        }

        if (mapperContext.hasModeButNot(TwinClassMode.Projection2TwinClassMode.HIDE)) {
            dst
                    .setDstTwinClassId(src.getDstTwinClassId());

            twinClassRestDTOMapper.postpone(src.getDstTwinClass(), mapperContext.forkOnPoint(mapperContext.getModeOrUse(TwinClassMode.Projection2TwinClassMode.SHORT)));
        }
    }
}
