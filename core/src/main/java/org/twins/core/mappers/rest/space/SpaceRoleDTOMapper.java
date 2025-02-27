package org.twins.core.mappers.rest.space;

import lombok.RequiredArgsConstructor;
import org.twins.core.i18n.service.I18nService;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModeBinding;
import org.twins.core.dao.space.SpaceRoleEntity;
import org.twins.core.dto.rest.space.SpaceRoleDTOv1;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.modes.SpaceRoleMode;


@Component
@RequiredArgsConstructor
@MapperModeBinding(modes = SpaceRoleMode.class)
public class SpaceRoleDTOMapper extends RestSimpleDTOMapper<SpaceRoleEntity, SpaceRoleDTOv1> {

    private final I18nService i18nService;

    @Override
    public void map(SpaceRoleEntity src, SpaceRoleDTOv1 dst, MapperContext mapperContext) throws Exception {
        switch (mapperContext.getModeOrUse(SpaceRoleMode.DETAILED)) {
            case DETAILED:
                dst
                        .setId(src.getId())
                        .setKey(src.getKey())
                        .setName(src.getNameI18NId() != null ? i18nService.translateToLocale(src.getNameI18NId()) : "")
                        .setDescription(src.getDescriptionI18NId() != null ? i18nService.translateToLocale(src.getDescriptionI18NId()) : "")
                        .setTwinClassId(src.getTwinClassId())
                        .setBusinessAccountId(src.getBusinessAccountId());
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
        return mapperContext.hasModeOrEmpty(SpaceRoleMode.HIDE);
    }

    @Override
    public String getObjectCacheId(SpaceRoleEntity src) {
        return src.getId().toString();
    }
}
