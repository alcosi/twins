package org.twins.core.mappers.rest.space;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.cambium.i18n.service.I18nService;
import org.springframework.stereotype.Component;
import org.twins.core.dao.space.SpaceRoleUserEntity;
import org.twins.core.dto.rest.space.SpaceRoleDTOv1;
import org.twins.core.mappers.rest.MapperContext;
import org.twins.core.mappers.rest.MapperMode;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;


@Component
@RequiredArgsConstructor
public class SpaceRoleByUserDTOMapper extends RestSimpleDTOMapper<SpaceRoleUserEntity, SpaceRoleDTOv1> {
    final I18nService i18nService;
    @Override
    public void map(SpaceRoleUserEntity src, SpaceRoleDTOv1 dst, MapperContext mapperContext) throws Exception {
        switch (mapperContext.getModeOrUse(Mode.DETAILED)) {
            case DETAILED:
                dst
                        .id(src.getId())
                        .key(src.getSpaceRole().getKey())
                        .name(src.getSpaceRole().getNameI18NId() != null ? i18nService.translateToLocale(src.getSpaceRole().getNameI18NId()) : "")
                        .description(src.getSpaceRole().getDescriptionI18NId() != null ? i18nService.translateToLocale(src.getSpaceRole().getDescriptionI18NId()) : "");
                break;
            case SHORT:
                dst
                        .id(src.getId())
                        .key(src.getSpaceRole().getKey());
                break;
        }
    }

    @Override
    public boolean hideMode(MapperContext mapperContext) {
        return mapperContext.hasModeOrEmpty(Mode.HIDE);
    }

    @AllArgsConstructor
    public enum Mode implements MapperMode {
        HIDE(0),
        SHORT(1),
        DETAILED(2);

        public static final String _HIDE = "HIDE";
        public static final String _SHORT = "SHORT";
        public static final String _DETAILED = "DETAILED";

        @Getter
        final int priority;
    }

    @Override
    public String getObjectCacheId(SpaceRoleUserEntity src) {
        return src.getId().toString();
    }
}
