package org.twins.core.mappers.rest.permission;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModeBinding;
import org.twins.core.controller.rest.annotation.MapperModePointerBinding;
import org.twins.core.dao.permission.PermissionGrantUserEntity;
import org.twins.core.dto.rest.permission.PermissionGrantUserDTOv2;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.PermissionMode;
import org.twins.core.mappers.rest.mappercontext.modes.PermissionSchemaMode;
import org.twins.core.mappers.rest.mappercontext.modes.PermissionGrantUserMode;
import org.twins.core.mappers.rest.mappercontext.modes.UserMode;
import org.twins.core.mappers.rest.user.UserRestDTOMapper;

@Component
@RequiredArgsConstructor
@MapperModeBinding(modes = PermissionGrantUserMode.class)
public class PermissionGrantUserRestDTOMapperV2 extends RestSimpleDTOMapper<PermissionGrantUserEntity, PermissionGrantUserDTOv2> {
    private final PermissionGrantUserRestDTOMapper permissionGrantUserRestDTOMapper;

    @MapperModePointerBinding(modes = PermissionSchemaMode.PermissionGrantUser2PermissionSchemaMode.class)
    private final PermissionSchemaRestDTOMapper permissionSchemaRestDTOMapper;

    @MapperModePointerBinding(modes = PermissionMode.PermissionGrantUser2PermissionMode.class)
    private final PermissionRestDTOMapperV2 permissionRestDTOMapperV2;

    @MapperModePointerBinding(modes = UserMode.PermissionGrantUser2UserMode.class)
    private final UserRestDTOMapper userRestDTOMapper;

    @Override
    public void map(PermissionGrantUserEntity src, PermissionGrantUserDTOv2 dst, MapperContext mapperContext) throws Exception {
        permissionGrantUserRestDTOMapper.map(src, dst, mapperContext);
        if (mapperContext.hasModeButNot(PermissionSchemaMode.PermissionGrantUser2PermissionSchemaMode.HIDE))
            dst
                    .setPermissionSchema(permissionSchemaRestDTOMapper.convertOrPostpone(src.getPermissionSchema(), mapperContext.forkOnPoint(mapperContext.getModeOrUse(PermissionSchemaMode.PermissionGrantUser2PermissionSchemaMode.SHORT))))
                    .setPermissionSchemaId(src.getPermissionSchemaId());
        if (mapperContext.hasModeButNot(PermissionMode.PermissionGrantUser2PermissionMode.HIDE))
            dst
                    .setPermission(permissionRestDTOMapperV2.convertOrPostpone(src.getPermission(), mapperContext.forkOnPoint(mapperContext.getModeOrUse(PermissionMode.PermissionGrantUser2PermissionMode.SHORT))))
                    .setPermissionId(src.getPermissionId());
        if (mapperContext.hasModeButNot(UserMode.PermissionGrantUser2UserMode.HIDE))
            dst
                    .setUser(userRestDTOMapper.convertOrPostpone(src.getUser(), mapperContext.forkOnPoint(mapperContext.getModeOrUse(UserMode.PermissionGrantUser2UserMode.SHORT))))
                    .setUserId(src.getUserId());
        if (mapperContext.hasModeButNot(UserMode.PermissionGrantUser2UserMode.HIDE))
            dst
                    .setGrantedByUser(userRestDTOMapper.convertOrPostpone(src.getGrantedByUser(), mapperContext.forkOnPoint(mapperContext.getModeOrUse(UserMode.PermissionGrantUser2UserMode.SHORT))))
                    .setGrantedByUserId(src.getGrantedByUserId());
    }

    @Override
    public boolean hideMode(MapperContext mapperContext) {
        return mapperContext.hasModeOrEmpty(PermissionGrantUserMode.HIDE);
    }

    @Override
    public String getObjectCacheId(PermissionGrantUserEntity src) {
        return src.getId().toString();
    }
}
