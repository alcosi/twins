package org.twins.core.mappers.rest.twinstatus;

import lombok.RequiredArgsConstructor;
import org.cambium.common.exception.ServiceException;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModeBinding;
import org.twins.core.dao.twin.TwinStatusEntity;
import org.twins.core.dto.rest.twinstatus.TwinStatusDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.StatusMode;
import org.twins.core.service.i18n.I18nService;
import org.twins.core.service.resource.ResourceService;


@Component
@RequiredArgsConstructor
@MapperModeBinding(modes = {StatusMode.class})
public class TwinStatusRestDTOMapper extends RestSimpleDTOMapper<TwinStatusEntity, TwinStatusDTOv1> {
    private final ResourceService resourceService;
    private final I18nService i18nService;

    @Override
    public void map(TwinStatusEntity src, TwinStatusDTOv1 dst, MapperContext mapperContext) throws ServiceException {
        switch (mapperContext.getModeOrUse(StatusMode.DETAILED)) {
            case DETAILED:
                dst
                        .setId(src.getId())
                        .setName(i18nService.translateToLocale(src.getNameI18nId()))
                        .setKey(src.getKey())
                        .setTwinClassId(src.getTwinClassId())
                        .setDescription(src.getDescriptionI18nId() != null ? i18nService.translateToLocale(src.getDescriptionI18nId()) : "")
                        .setIconDark(resourceService.getResourceUri(src.getIconDarkResource()))
                        .setIconLight(resourceService.getResourceUri(src.getIconLightResource()))
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
