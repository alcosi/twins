package org.twins.core.mappers.rest.permission;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModeBinding;
import org.twins.core.controller.rest.annotation.MapperModePointerBinding;
import org.twins.core.dao.permission.PermissionEntity;
import org.twins.core.dto.rest.permission.PermissionDTOv1;
import org.twins.core.holder.I18nCacheHolder;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.PermissionGroupMode;
import org.twins.core.mappers.rest.mappercontext.modes.PermissionMode;
import org.twins.core.service.i18n.I18nService;

@Component
@RequiredArgsConstructor
@MapperModeBinding(modes = PermissionMode.class)
public class PermissionRestDTOMapper extends RestSimpleDTOMapper<PermissionEntity, PermissionDTOv1> {
    private final I18nService i18nService;

    @MapperModePointerBinding(modes = PermissionGroupMode.Permission2PermissionGroupMode.class)
    private final PermissionGroupRestDTOMapper permissionGroupRestDTOMapper;

    @Override
    public void map(PermissionEntity src, PermissionDTOv1 dst, MapperContext mapperContext) {
        switch (mapperContext.getModeOrUse(PermissionMode.DETAILED)) {
            case DETAILED:
                dst
                        .setId(src.getId())
                        .setKey(src.getKey())
                        .setName(I18nCacheHolder.addId(src.getNameI18NId()))
                        .setDescription(I18nCacheHolder.addId(src.getDescriptionI18NId()))
                        .setGroupId(src.getPermissionGroupId());
                break;
            case SHORT:
                dst
                        .setId(src.getId())
                        .setKey(src.getKey());
                break;
        }
        if (showPermissionGroup(mapperContext)) {
            dst.setGroupId(src.getPermissionGroupId());
            permissionGroupRestDTOMapper.postpone(src.getPermissionGroup(), mapperContext.forkOnPoint(PermissionGroupMode.Permission2PermissionGroupMode.SHORT));
        }
    }

    private static boolean showPermissionGroup(MapperContext mapperContext) {
        return mapperContext.hasModeButNot(PermissionGroupMode.Permission2PermissionGroupMode.HIDE);
    }

    @Override
    public boolean hideMode(MapperContext mapperContext) {
        return mapperContext.hasModeOrEmpty(PermissionMode.HIDE);
    }

    @Override
    public String getObjectCacheId(PermissionEntity src) {
        return src.getId().toString();
    }
}
