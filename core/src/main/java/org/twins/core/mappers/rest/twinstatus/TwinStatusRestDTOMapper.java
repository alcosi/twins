package org.twins.core.mappers.rest.twinstatus;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.cambium.i18n.service.I18nService;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinStatusEntity;
import org.twins.core.dto.rest.twin.TwinStatusDTOv1;
import org.twins.core.mappers.rest.MapperContext;
import org.twins.core.mappers.rest.MapperMode;
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
                        .setId(src.getId())
                        .setName(i18nService.translateToLocale(src.getNameI18nId()))
                        .setDescription(src.getDescriptionI18nId() != null ? i18nService.translateToLocale(src.getDescriptionI18nId()) : "")
                        .setLogo(src.getLogo())
                        .setColor(src.getColor());
                break;
            case SHORT:
                dst
                        .setId(src.getId())
                        .setName(i18nService.translateToLocale(src.getNameI18nId()));
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
}
