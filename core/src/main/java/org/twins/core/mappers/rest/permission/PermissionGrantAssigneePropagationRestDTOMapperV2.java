package org.twins.core.mappers.rest.permission;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModePointerBinding;
import org.twins.core.dao.permission.PermissionGrantAssigneePropagationEntity;
import org.twins.core.dto.rest.permission.PermissionGrantAssigneePropagationDTOv2;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.*;
import org.twins.core.mappers.rest.twinclass.TwinClassRestDTOMapper;
import org.twins.core.mappers.rest.twinstatus.TwinStatusRestDTOMapper;
import org.twins.core.mappers.rest.user.UserRestDTOMapper;

@Component
@RequiredArgsConstructor
public class PermissionGrantAssigneePropagationRestDTOMapperV2 extends RestSimpleDTOMapper<PermissionGrantAssigneePropagationEntity, PermissionGrantAssigneePropagationDTOv2> {

    private final PermissionGrantAssigneePropagationRestDTOMapper permissionGrantAssigneePropagationRestDTOMapper;

    @MapperModePointerBinding(modes = PermissionSchemaMode.PermissionGrantAssigneePropagation2PermissionSchemaMode.class)
    private final PermissionSchemaRestDTOMapper permissionSchemaRestDTOMapper;

    @MapperModePointerBinding(modes = PermissionMode.PermissionGrantAssigneePropagation2PermissionMode.class)
    private final PermissionRestDTOMapperV2 permissionRestDTOMapperV2;

    @MapperModePointerBinding(modes = TwinClassMode.PermissionGrantAssigneePropagation2TwinClassMode.class)
    private final TwinClassRestDTOMapper twinClassRestDTOMapper;

    @MapperModePointerBinding(modes = StatusMode.PropagationTwinStatus2StatusMode.class)
    private final TwinStatusRestDTOMapper twinStatusRestDTOMapper;

    @MapperModePointerBinding(modes = UserMode.PermissionGrantAssigneePropagation2UserMode.class)
    private final UserRestDTOMapper userRestDTOMapper;

    @Override
    public void map(PermissionGrantAssigneePropagationEntity src, PermissionGrantAssigneePropagationDTOv2 dst, MapperContext mapperContext) throws Exception {
        permissionGrantAssigneePropagationRestDTOMapper.map(src, dst, mapperContext);
        if (mapperContext.hasModeButNot(PermissionSchemaMode.PermissionGrantAssigneePropagation2PermissionSchemaMode.HIDE))
            dst
                    .setPermissionSchema(permissionSchemaRestDTOMapper.convertOrPostpone(src.getPermissionSchema(), mapperContext.forkOnPoint(PermissionSchemaMode.PermissionGrantAssigneePropagation2PermissionSchemaMode.SHORT)))
                    .setPermissionSchemaId(src.getPermissionSchemaId());
        if (mapperContext.hasModeButNot(PermissionMode.PermissionGrantAssigneePropagation2PermissionMode.HIDE))
            dst
                    .setPermission(permissionRestDTOMapperV2.convertOrPostpone(src.getPermission(), mapperContext.forkOnPoint(PermissionMode.PermissionGrantAssigneePropagation2PermissionMode.SHORT)))
                    .setPermissionId(src.getPermissionId());
        if (mapperContext.hasModeButNot(TwinClassMode.PermissionGrantAssigneePropagation2TwinClassMode.HIDE))
            dst
                    .setPropagationTwinClass(twinClassRestDTOMapper.convertOrPostpone(src.getTwinClass(), mapperContext.forkOnPoint(TwinClassMode.PermissionGrantAssigneePropagation2TwinClassMode.SHORT)))
                    .setPropagationTwinClassId(src.getPropagationByTwinClassId());
        if (mapperContext.hasModeButNot(StatusMode.PropagationTwinStatus2StatusMode.HIDE))
            dst
                    .setPropagationTwinStatus(twinStatusRestDTOMapper.convertOrPostpone(src.getTwinStatus(), mapperContext.forkOnPoint(StatusMode.PropagationTwinStatus2StatusMode.SHORT)))
                    .setPropagationTwinStatusId(src.getPropagationByTwinStatusId());
        if (mapperContext.hasModeButNot(UserMode.PermissionGrantAssigneePropagation2UserMode.HIDE))
            dst
                    .setGrantedByUser(userRestDTOMapper.convertOrPostpone(src.getGrantedByUser(), mapperContext.forkOnPoint(UserMode.PermissionGrantAssigneePropagation2UserMode.SHORT)))
                    .setGrantedByUserId(src.getGrantedByUserId());
    }
}
