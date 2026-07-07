package org.twins.core.mappers.rest.permission;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModeBinding;
import org.twins.core.controller.rest.annotation.MapperModePointerBinding;
import org.twins.core.dao.permission.PermissionGrantUserEntity;
import org.twins.core.dto.rest.permission.PermissionGrantUserDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.PermissionGrantUserMode;
import org.twins.core.mappers.rest.mappercontext.modes.PermissionMode;
import org.twins.core.mappers.rest.mappercontext.modes.PermissionSchemaMode;
import org.twins.core.mappers.rest.mappercontext.modes.UserMode;
import org.twins.core.mappers.rest.user.UserRestDTOMapper;
import org.twins.core.service.permission.PermissionGrantUserService;

import java.util.Collection;

@Component
@RequiredArgsConstructor
@MapperModeBinding(modes = PermissionGrantUserMode.class)
public class PermissionGrantUserRestDTOMapper extends RestSimpleDTOMapper<PermissionGrantUserEntity, PermissionGrantUserDTOv1> {
    @MapperModePointerBinding(modes = PermissionSchemaMode.PermissionGrantUser2PermissionSchemaMode.class)
    private final PermissionSchemaRestDTOMapper permissionSchemaRestDTOMapper;

    @MapperModePointerBinding(modes = PermissionMode.PermissionGrantUser2PermissionMode.class)
    private final PermissionRestDTOMapper permissionRestDTOMapper;

    @MapperModePointerBinding(modes = UserMode.PermissionGrantUser2UserMode.class)
    private final UserRestDTOMapper userRestDTOMapper;

    private final PermissionGrantUserService permissionGrantUserService;

    @Override
    public void map(PermissionGrantUserEntity src, PermissionGrantUserDTOv1 dst, MapperContext mapperContext) throws Exception {
        switch (mapperContext.getModeOrUse(PermissionGrantUserMode.DETAILED)) {
            case DETAILED:
                dst
                        .setId(src.getId())
                        .setPermissionSchemaId(src.getPermissionSchemaId())
                        .setPermissionId(src.getPermissionId())
                        .setUserId(src.getUserId())
                        .setGrantedByUserId(src.getGrantedByUserId())
                        .setGrantedAt(src.getGrantedAt().toLocalDateTime());
                break;
            case SHORT:
                dst
                        .setId(src.getId())
                        .setPermissionId(src.getPermissionId())
                        .setUserId(src.getUserId());
                break;
        }
        if (mapperContext.hasModeButNot(PermissionSchemaMode.PermissionGrantUser2PermissionSchemaMode.HIDE)) {
            dst.setPermissionSchemaId(src.getPermissionSchemaId());
            permissionSchemaRestDTOMapper.postpone(src.getPermissionSchema(), mapperContext.forkOnPoint(mapperContext.getModeOrUse(PermissionSchemaMode.PermissionGrantUser2PermissionSchemaMode.SHORT)));
        }
        if (mapperContext.hasModeButNot(PermissionMode.PermissionGrantUser2PermissionMode.HIDE)) {
            permissionGrantUserService.loadPermission(src);
            dst.setPermissionId(src.getPermissionId());
            permissionRestDTOMapper.postpone(src.getPermission(), mapperContext.forkOnPoint(mapperContext.getModeOrUse(PermissionMode.PermissionGrantUser2PermissionMode.SHORT)));
        }
        if (mapperContext.hasModeButNot(UserMode.PermissionGrantUser2UserMode.HIDE)) {
            permissionGrantUserService.loadUser(src);
            dst.setUserId(src.getUserId());
            userRestDTOMapper.postpone(src.getUser(), mapperContext.forkOnPoint(mapperContext.getModeOrUse(UserMode.PermissionGrantUser2UserMode.SHORT)));
        }
        if (mapperContext.hasModeButNot(UserMode.PermissionGrantUser2UserMode.HIDE)) {
            permissionGrantUserService.loadGrantedByUser(src);
            dst.setGrantedByUserId(src.getGrantedByUserId());
            userRestDTOMapper.postpone(src.getGrantedByUser(), mapperContext.forkOnPoint(mapperContext.getModeOrUse(UserMode.PermissionGrantUser2UserMode.SHORT)));
        }
    }

    @Override
    public void beforeCollectionConversion(Collection<PermissionGrantUserEntity> srcCollection, MapperContext mapperContext) throws Exception {
        super.beforeCollectionConversion(srcCollection, mapperContext);
        if (mapperContext.hasModeButNot(PermissionMode.PermissionGrantUser2PermissionMode.HIDE))
            permissionGrantUserService.loadPermission(srcCollection);
        if (mapperContext.hasModeButNot(UserMode.PermissionGrantUser2UserMode.HIDE)) {
            permissionGrantUserService.loadUser(srcCollection);
            permissionGrantUserService.loadGrantedByUser(srcCollection);
        }
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
