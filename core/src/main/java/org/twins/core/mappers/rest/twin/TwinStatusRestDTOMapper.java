package org.twins.core.mappers.rest.twin;

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
        switch (mapperContext.getModeOrUse(MapperMode.StatusMode.DETAILED)) {
            case DETAILED:
                dst
                        .id(src.getId())
                        .name(i18nService.translateToLocale(src.getNameI18nId()))
                        .description(src.getDescriptionI18nId() != null ? i18nService.translateToLocale(src.getDescriptionI18nId()) : "")
                        .logo(src.getLogo())
                        .color(src.getColor());
                break;
            case SHORT:
                dst
                        .id(src.getId())
                        .name(i18nService.translateToLocale(src.getNameI18nId()));
                break;
        }
    }

    @Override
    public boolean hideMode(MapperContext mapperContext) {
        return mapperContext.hasModeOrEmpty(MapperMode.StatusMode.HIDE);
    }

    @Override
    public String getObjectCacheId(TwinStatusEntity src) {
        return src.getId().toString();
    }

}
