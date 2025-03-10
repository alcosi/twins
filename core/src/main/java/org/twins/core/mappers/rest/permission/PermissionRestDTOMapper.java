package org.twins.core.mappers.rest.permission;

import lombok.RequiredArgsConstructor;
import org.twins.core.service.i18n.I18nService;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModeBinding;
import org.twins.core.dao.permission.PermissionEntity;
import org.twins.core.dto.rest.permission.PermissionDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.PermissionMode;

@Component
@RequiredArgsConstructor
@MapperModeBinding(modes = PermissionMode.class)
public class PermissionRestDTOMapper extends RestSimpleDTOMapper<PermissionEntity, PermissionDTOv1> {
    private final I18nService i18nService;

    @Override
    public void map(PermissionEntity src, PermissionDTOv1 dst, MapperContext mapperContext) {
        switch (mapperContext.getModeOrUse(PermissionMode.DETAILED)) {
            case DETAILED:
                dst
                        .setId(src.getId())
                        .setKey(src.getKey())
                        .setName(i18nService.translateToLocale(src.getNameI18NId()))
                        .setDescription(i18nService.translateToLocale(src.getDescriptionI18NId()))
                        .setGroupId(src.getPermissionGroupId());
                break;
            case SHORT:
                dst
                        .setId(src.getId())
                        .setKey(src.getKey());
                break;
        }
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
