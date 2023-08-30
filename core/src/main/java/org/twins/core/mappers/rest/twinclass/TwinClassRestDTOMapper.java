package org.twins.core.mappers.rest.twinclass;

import lombok.RequiredArgsConstructor;
import org.cambium.i18n.service.I18nService;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.dto.rest.twinclass.TwinClassDTOv1;
import org.twins.core.mappers.rest.MapperMode;
import org.twins.core.mappers.rest.MapperProperties;
import org.twins.core.mappers.rest.RestListDTOMapper;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.user.UserDTOMapper;


@Component
@RequiredArgsConstructor
public class TwinClassRestDTOMapper extends RestSimpleDTOMapper<TwinClassEntity, TwinClassDTOv1> {
    final I18nService i18nService;

    @Override
    public void map(TwinClassEntity src, TwinClassDTOv1 dst, MapperProperties mapperProperties) {
        switch (mapperProperties.getModeOrUse(TwinClassRestDTOMapper.Mode.DETAILED)) {
            case ID_ONLY:
                dst
                        .id(src.id());
                break;
            case DETAILED:
                dst
                        .id(src.id())
                        .key(src.key())
                        .name(i18nService.translateToLocale(src.nameI18n()))
                        .description(src.descriptionI18n() != null ? i18nService.translateToLocale(src.descriptionI18n()) : "")
                        .logo(src.logo())
                ;
                break;
            default:
                dst
                        .id(src.id())
                        .key(src.key())
                        .name(i18nService.translateToLocale(src.nameI18n()));
        }
    }

    public enum Mode implements MapperMode {
        ID_ONLY, DETAILED;
    }
}
