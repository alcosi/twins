package org.twins.core.mappers.rest.twinclass;

import lombok.RequiredArgsConstructor;
import org.cambium.i18n.service.I18nService;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.dto.rest.twinclass.TwinClassBaseDTOv1;
import org.twins.core.mappers.rest.MapperContext;
import org.twins.core.mappers.rest.MapperMode;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;


@Component
@RequiredArgsConstructor
public class TwinClassBaseRestDTOMapper extends RestSimpleDTOMapper<TwinClassEntity, TwinClassBaseDTOv1> {
    final I18nService i18nService;

    @Override
    public void map(TwinClassEntity src, TwinClassBaseDTOv1 dst, MapperContext mapperContext) throws Exception {
        switch (mapperContext.getModeOrUse(ClassMode.DETAILED)) {
            case DETAILED:
                dst
                        .id(src.getId())
                        .key(src.getKey())
                        .headClassId(src.getHeadTwinClassId())
//                        .headClass(convertOrPostpone(src))
                        .abstractClass(src.isAbstractt())
                        .name(i18nService.translateToLocale(src.getNameI18NId()))
                        .description(src.getDescriptionI18NId() != null ? i18nService.translateToLocale(src.getDescriptionI18NId()) : "")
                        .logo(src.getLogo());
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
        return mapperContext.hasModeOrEmpty(ClassMode.HIDE);
    }

    @Override
    public String getObjectCacheId(TwinClassEntity src) {
        return src.getId().toString();
    }

    public enum ClassMode implements MapperMode {
        SHORT, DETAILED, HIDE;

        public static final String _SHORT = "SHORT";
        public static final String _DETAILED = "DETAILED";
        public static final String _HIDE = "HIDE";
    }
}