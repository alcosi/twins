package org.twins.core.mappers.rest.twinclass;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModeBinding;
import org.twins.core.dao.twinclass.TwinClassAvailabilityEntity;
import org.twins.core.dto.rest.twinclass.TwinClassAvailabilityDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.TwinClassAvailabilityMode;
import org.twins.core.mappers.rest.mappercontext.modes.TwinClassMode;
import org.twins.core.service.i18n.I18nService;

@Slf4j
@Component
@RequiredArgsConstructor
@MapperModeBinding(modes = {TwinClassAvailabilityMode.class})
public class TwinClassAvailabilityDTOMapper extends RestSimpleDTOMapper<TwinClassAvailabilityEntity, TwinClassAvailabilityDTOv1> {

    private final I18nService i18nService;

    @Override
    public void map(TwinClassAvailabilityEntity src, TwinClassAvailabilityDTOv1 dst, MapperContext mapperContext) throws Exception {
        switch (mapperContext.getModeOrUse(TwinClassAvailabilityMode.DETAILED)) {
            case DETAILED:
                dst
                        .setId(src.getId())
                        .setKey(src.getKey())
                        .setName(i18nService.translateToLocale(src.getNameI18NId()))
                        .setDescription(src.getDescriptionI18NId() != null ? i18nService.translateToLocale(src.getDescriptionI18NId()) : "")
                ;
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
        return mapperContext.hasModeOrEmpty(TwinClassMode.HIDE);
    }

    @Override
    public String getObjectCacheId(TwinClassAvailabilityEntity src) {
        return src.getId().toString();
    }
}
