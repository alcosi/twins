package org.twins.core.mappers.rest.permission;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModeBinding;
import org.twins.core.controller.rest.annotation.MapperModePointerBinding;
import org.twins.core.dao.permission.PermissionGrantAssigneePropagationEntity;
import org.twins.core.dto.rest.permission.PermissionGrantAssigneePropagationDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.*;
import org.twins.core.mappers.rest.twinclass.TwinClassRestDTOMapper;
import org.twins.core.mappers.rest.twinstatus.TwinStatusRestDTOMapper;
import org.twins.core.mappers.rest.user.UserRestDTOMapper;

@Component
@RequiredArgsConstructor
@MapperModeBinding(modes = PermissionGrantAssigneePropagationMode.class)
public class PermissionGrantAssigneePropagationRestDTOMapper extends RestSimpleDTOMapper<PermissionGrantAssigneePropagationEntity, PermissionGrantAssigneePropagationDTOv1> {

    @MapperModePointerBinding(modes = PermissionSchemaMode.PermissionGrantAssigneePropagation2PermissionSchemaMode.class)
    private final PermissionSchemaRestDTOMapper permissionSchemaRestDTOMapper;

    @MapperModePointerBinding(modes = PermissionMode.PermissionGrantAssigneePropagation2PermissionMode.class)
    private final PermissionRestDTOMapper permissionRestDTOMapper;

    @MapperModePointerBinding(modes = TwinClassMode.PermissionGrantAssigneePropagation2TwinClassMode.class)
    private final TwinClassRestDTOMapper twinClassRestDTOMapper;

    @MapperModePointerBinding(modes = StatusMode.PropagationTwinStatus2StatusMode.class)
    private final TwinStatusRestDTOMapper twinStatusRestDTOMapper;

    @MapperModePointerBinding(modes = UserMode.PermissionGrantAssigneePropagation2UserMode.class)
    private final UserRestDTOMapper userRestDTOMapper;

    @Override
    public void map(PermissionGrantAssigneePropagationEntity src, PermissionGrantAssigneePropagationDTOv1 dst, MapperContext mapperContext) throws Exception {
        switch (mapperContext.getModeOrUse(PermissionGrantAssigneePropagationMode.DETAILED)) {
            case DETAILED:
                dst
                        .setId(src.getId())
                        .setPermissionSchemaId(src.getPermissionSchemaId())
                        .setPermissionId(src.getPermissionId())
                        .setPropagationTwinClassId(src.getPropagationByTwinClassId())
                        .setPropagationTwinStatusId(src.getPropagationByTwinStatusId())
                        .setGrantedByUserId(src.getGrantedByUserId())
                        .setGrantedAt(src.getGrantedAt().toLocalDateTime());
                break;
            case SHORT:
                dst
                        .setId(src.getId())
                        .setPermissionId(src.getPermissionId())
                        .setPropagationTwinClassId(src.getPropagationByTwinClassId());
                break;
        }

        if (mapperContext.hasModeButNot(PermissionSchemaMode.PermissionGrantAssigneePropagation2PermissionSchemaMode.HIDE)) {
            dst.setPermissionSchemaId(src.getPermissionSchemaId());
            permissionSchemaRestDTOMapper.postpone(src.getPermissionSchema(), mapperContext.forkOnPoint(PermissionSchemaMode.PermissionGrantAssigneePropagation2PermissionSchemaMode.SHORT));
        }

        if (mapperContext.hasModeButNot(PermissionMode.PermissionGrantAssigneePropagation2PermissionMode.HIDE)) {
            dst.setPermissionSchemaId(src.getPermissionId());
            permissionRestDTOMapper.postpone(src.getPermission(), mapperContext.forkOnPoint(PermissionMode.PermissionGrantAssigneePropagation2PermissionMode.SHORT));
        }

        if (mapperContext.hasModeButNot(TwinClassMode.PermissionGrantAssigneePropagation2TwinClassMode.HIDE)) {
            dst.setPropagationTwinClassId(src.getPropagationByTwinClassId());
            twinClassRestDTOMapper.postpone(src.getTwinClass(), mapperContext.forkOnPoint(TwinClassMode.PermissionGrantAssigneePropagation2TwinClassMode.SHORT));
        }

        if (mapperContext.hasModeButNot(StatusMode.PropagationTwinStatus2StatusMode.HIDE)) {
            dst.setPropagationTwinStatusId(src.getPropagationByTwinStatusId());
            twinStatusRestDTOMapper.postpone(src.getTwinStatus(), mapperContext.forkOnPoint(StatusMode.PropagationTwinStatus2StatusMode.SHORT));
        }

        if (mapperContext.hasModeButNot(UserMode.PermissionGrantAssigneePropagation2UserMode.HIDE)) {
            dst.setGrantedByUserId(src.getGrantedByUserId());
            userRestDTOMapper.postpone(src.getGrantedByUser(), mapperContext.forkOnPoint(UserMode.PermissionGrantAssigneePropagation2UserMode.SHORT));
        }
    }

    @Override
    public String getObjectCacheId(PermissionGrantAssigneePropagationEntity src) {
        return src.getId().toString();
    }
}
