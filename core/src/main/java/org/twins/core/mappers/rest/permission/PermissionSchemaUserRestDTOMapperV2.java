package org.twins.core.mappers.rest.permission;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModeBinding;
import org.twins.core.controller.rest.annotation.MapperModePointerBinding;
import org.twins.core.dao.permission.PermissionSchemaUserEntity;
import org.twins.core.dto.rest.permission.PermissionSchemaUserDTOv2;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.PermissionMode;
import org.twins.core.mappers.rest.mappercontext.modes.PermissionSchemaMode;
import org.twins.core.mappers.rest.mappercontext.modes.PermissionSchemaUserMode;
import org.twins.core.mappers.rest.mappercontext.modes.UserMode;
import org.twins.core.mappers.rest.user.UserRestDTOMapper;

@Component
@RequiredArgsConstructor
@MapperModeBinding(modes = PermissionSchemaUserMode.class)
public class PermissionSchemaUserRestDTOMapperV2 extends RestSimpleDTOMapper<PermissionSchemaUserEntity, PermissionSchemaUserDTOv2> {
    private final PermissionSchemaUserRestDTOMapper permissionSchemaUserRestDTOMapper;

    @MapperModePointerBinding(modes = PermissionSchemaMode.PermissionSchemaUser2PermissionSchemaMode.class)
    private final PermissionSchemaRestDTOMapper permissionSchemaRestDTOMapper;

    @MapperModePointerBinding(modes = PermissionMode.PermissionSchemaUser2PermissionMode.class)
    private final PermissionRestDTOMapperV2 permissionRestDTOMapperV2;

    @MapperModePointerBinding(modes = UserMode.PermissionSchemaUser2UserMode.class)
    private final UserRestDTOMapper userRestDTOMapper;

    @Override
    public void map(PermissionSchemaUserEntity src, PermissionSchemaUserDTOv2 dst, MapperContext mapperContext) throws Exception {
        permissionSchemaUserRestDTOMapper.map(src, dst, mapperContext);
        if (mapperContext.hasModeButNot(PermissionSchemaMode.PermissionSchemaUser2PermissionSchemaMode.HIDE))
            dst
                    .setPermissionSchema(permissionSchemaRestDTOMapper.convertOrPostpone(src.getPermissionSchema(), mapperContext.forkOnPoint(mapperContext.getModeOrUse(PermissionSchemaMode.PermissionSchemaUser2PermissionSchemaMode.SHORT))))
                    .setPermissionSchemaId(src.getPermissionSchemaId());
        if (mapperContext.hasModeButNot(PermissionMode.PermissionSchemaUser2PermissionMode.HIDE))
            dst
                    .setPermission(permissionRestDTOMapperV2.convertOrPostpone(src.getPermission(), mapperContext.forkOnPoint(mapperContext.getModeOrUse(PermissionMode.PermissionSchemaUser2PermissionMode.SHORT))))
                    .setPermissionId(src.getPermissionId());
        if (mapperContext.hasModeButNot(UserMode.PermissionSchemaUser2UserMode.HIDE))
            dst
                    .setUser(userRestDTOMapper.convertOrPostpone(src.getUser(), mapperContext.forkOnPoint(mapperContext.getModeOrUse(UserMode.PermissionSchemaUser2UserMode.SHORT))))
                    .setUserId(src.getUserId());
        if (mapperContext.hasModeButNot(UserMode.PermissionSchemaUser2UserMode.HIDE))
            dst
                    .setGrantedByUser(userRestDTOMapper.convertOrPostpone(src.getGrantedByUser(), mapperContext.forkOnPoint(mapperContext.getModeOrUse(UserMode.PermissionSchemaUser2UserMode.SHORT))))
                    .setGrantedByUserId(src.getGrantedByUserId());
    }

    @Override
    public boolean hideMode(MapperContext mapperContext) {
        return mapperContext.hasModeOrEmpty(PermissionSchemaUserMode.HIDE);
    }

    @Override
    public String getObjectCacheId(PermissionSchemaUserEntity src) {
        return src.getId().toString();
    }
}
