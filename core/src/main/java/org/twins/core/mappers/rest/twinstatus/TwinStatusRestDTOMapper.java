package org.twins.core.mappers.rest.twinstatus;

import lombok.RequiredArgsConstructor;
import org.cambium.i18n.service.I18nService;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModeBinding;
import org.twins.core.dao.twin.TwinStatusEntity;
import org.twins.core.dto.rest.twinstatus.TwinStatusDTOv1;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.modes.StatusMode;


@Component
@RequiredArgsConstructor
@MapperModeBinding(modes = {StatusMode.class})
public class TwinStatusRestDTOMapper extends RestSimpleDTOMapper<TwinStatusEntity, TwinStatusDTOv1> {

    private final I18nService i18nService;

    @Override
    public void map(TwinStatusEntity src, TwinStatusDTOv1 dst, MapperContext mapperContext) {
        switch (mapperContext.getModeOrUse(StatusMode.DETAILED)) {
            case DETAILED:
                dst
                        .setId(src.getId())
                        .setName(i18nService.translateToLocale(src.getNameI18nId()))
                        .setKey(src.getKey())
                        .setDescription(src.getDescriptionI18nId() != null ? i18nService.translateToLocale(src.getDescriptionI18nId()) : "")
                        .setLogo(src.getLogo())
                        .setBackgroundColor(src.getBackgroundColor())
                        .setFontColor(src.getFontColor());
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
        return mapperContext.hasModeOrEmpty(StatusMode.HIDE);
    }

    @Override
    public String getObjectCacheId(TwinStatusEntity src) {
        return src.getId().toString();
    }

}
