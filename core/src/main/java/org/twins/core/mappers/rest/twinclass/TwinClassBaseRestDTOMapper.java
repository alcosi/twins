package org.twins.core.mappers.rest.twinclass;

import lombok.RequiredArgsConstructor;
import org.cambium.i18n.service.I18nService;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.dto.rest.twinclass.TwinClassBaseDTOv1;
import org.twins.core.mappers.rest.MapperMode;
import org.twins.core.mappers.rest.MapperProperties;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;


@Component
@RequiredArgsConstructor
public class TwinClassBaseRestDTOMapper extends RestSimpleDTOMapper<TwinClassEntity, TwinClassBaseDTOv1> {
    final I18nService i18nService;

    @Override
    public void map(TwinClassEntity src, TwinClassBaseDTOv1 dst, MapperProperties mapperProperties) throws Exception {
        switch (mapperProperties.getModeOrUse(ClassMode.DETAILED)) {
            case DETAILED:
                dst
                        .key(src.getKey())
                        .headClassId(src.getHeadTwinClassId())
//                        .headClass(convertOrPostpone(src))
                        .abstractClass(src.isAbstractt())
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
    public boolean hideMode(MapperProperties mapperProperties) {
        return mapperProperties.hasMode(ClassMode.HIDE);
    }

    public enum ClassMode implements MapperMode {
        ID_ONLY, DETAILED, HIDE;

        public static final String _ID_ONLY = "ID_ONLY";
        public static final String _DETAILED = "DETAILED";
        public static final String _HIDE = "HIDE";
    }
}
