package org.twins.core.mappers.rest.space;

import lombok.RequiredArgsConstructor;
import org.cambium.i18n.service.I18nService;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModeBinding;
import org.twins.core.dao.space.SpaceRoleEntity;
import org.twins.core.dto.rest.space.SpaceRoleDTOv1;
import org.twins.core.mappers.rest.MapperContext;
import org.twins.core.mappers.rest.MapperMode;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;


@Component
@RequiredArgsConstructor
@MapperModeBinding(modes = MapperMode.SpaceRoleMode.class)
public class SpaceRoleDTOMapper extends RestSimpleDTOMapper<SpaceRoleEntity, SpaceRoleDTOv1> {

    private final I18nService i18nService;

    @Override
    public void map(SpaceRoleEntity src, SpaceRoleDTOv1 dst, MapperContext mapperContext) throws Exception {
        switch (mapperContext.getModeOrUse(MapperMode.SpaceRoleMode.DETAILED)) {
            case DETAILED:
                dst
                        .id(src.getId())
                        .key(src.getKey())
                        .name(src.getNameI18NId() != null ? i18nService.translateToLocale(src.getNameI18NId()) : "")
                        .description(src.getDescriptionI18NId() != null ? i18nService.translateToLocale(src.getDescriptionI18NId()) : "");
                break;
            case SHORT:
                dst
                        .id(src.getId())
                        .key(src.getKey());
                break;
        }
    }

    @Override
    public boolean hideMode(MapperContext mapperContext) {
        return mapperContext.hasModeOrEmpty(MapperMode.SpaceRoleMode.HIDE);
    }

    @Override
    public String getObjectCacheId(SpaceRoleEntity src) {
        return src.getId().toString();
    }
}
