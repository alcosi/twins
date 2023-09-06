package org.twins.core.mappers.rest.twin;

import lombok.RequiredArgsConstructor;
import org.cambium.i18n.service.I18nService;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinStatusEntity;
import org.twins.core.dto.rest.twin.TwinStatusDTOv1;
import org.twins.core.mappers.rest.MapperMode;
import org.twins.core.mappers.rest.MapperProperties;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;


@Component
@RequiredArgsConstructor
public class TwinStatusRestDTOMapper extends RestSimpleDTOMapper<TwinStatusEntity, TwinStatusDTOv1> {
    final I18nService i18nService;

    @Override
    public void map(TwinStatusEntity src, TwinStatusDTOv1 dst, MapperProperties mapperProperties) {
        switch (mapperProperties.getModeOrUse(TwinStatusRestDTOMapper.Mode.DETAILED)) {
            case DETAILED:
                dst
                        .name(i18nService.translateToLocale(src.nameI18n()))
                        .description(src.descriptionI18n() != null ? i18nService.translateToLocale(src.descriptionI18n()) : "")
                        .logo(src.logo());
            case ID_ONLY:
                dst
                        .id(src.id());
                break;
        }
    }

    public enum Mode implements MapperMode {
        ID_ONLY, DETAILED;

        public static final String _ID_ONLY = "ID_ONLY";
        public static final String _DETAILED = "DETAILED";
    }
}
