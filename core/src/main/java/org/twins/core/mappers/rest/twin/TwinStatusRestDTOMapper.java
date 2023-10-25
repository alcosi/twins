package org.twins.core.mappers.rest.twin;

import lombok.RequiredArgsConstructor;
import org.cambium.i18n.service.I18nService;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinStatusEntity;
import org.twins.core.dto.rest.twin.TwinStatusDTOv1;
import org.twins.core.mappers.rest.MapperMode;
import org.twins.core.mappers.rest.MapperContext;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;


@Component
@RequiredArgsConstructor
public class TwinStatusRestDTOMapper extends RestSimpleDTOMapper<TwinStatusEntity, TwinStatusDTOv1> {
    final I18nService i18nService;

    @Override
    public void map(TwinStatusEntity src, TwinStatusDTOv1 dst, MapperContext mapperContext) {
        switch (mapperContext.getModeOrUse(TwinStatusRestDTOMapper.Mode.DETAILED)) {
            case DETAILED:
                dst
                        .name(i18nService.translateToLocale(src.getNameI18n()))
                        .description(src.getDescriptionI18n() != null ? i18nService.translateToLocale(src.getDescriptionI18n()) : "")
                        .logo(src.getLogo());
            case ID_ONLY:
                dst
                        .id(src.getId());
                break;
        }
    }

    @Override
    public boolean hideMode(MapperContext mapperContext) {
        return mapperContext.hasModeOrEmpty(Mode.HIDE);
    }

    @Override
    public String getObjectCacheId(TwinStatusEntity src) {
        return src.getId().toString();
    }

    public enum Mode implements MapperMode {
        ID_ONLY, DETAILED, HIDE;

        public static final String _ID_ONLY = "ID_ONLY";
        public static final String _DETAILED = "DETAILED";
        public static final String _HIDE = "HIDE";
    }
}
