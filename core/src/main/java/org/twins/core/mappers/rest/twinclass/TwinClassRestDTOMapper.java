package org.twins.core.mappers.rest.twinclass;

import lombok.RequiredArgsConstructor;
import org.cambium.i18n.service.I18nService;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.dto.rest.twinclass.TwinClassDTOv1;
import org.twins.core.mappers.rest.RestDTOMapper;


@Component
@RequiredArgsConstructor
public class TwinClassRestDTOMapper implements RestDTOMapper<TwinClassEntity, TwinClassDTOv1> {
    final I18nService i18nService;

    public TwinClassDTOv1 convert(TwinClassEntity twinClassEntity) {
        TwinClassDTOv1 twinClassDTOv1 = new TwinClassDTOv1();
        map(twinClassEntity, twinClassDTOv1);
        return twinClassDTOv1;
    }

    @Override
    public void map(TwinClassEntity src, TwinClassDTOv1 dst) {
        dst
                .id(src.getId())
                .key(src.getKey())
                .name(i18nService.translateToLocale(src.getNameI18n()))
                .description(src.getDescriptionI18n() != null ? i18nService.translateToLocale(src.getDescriptionI18n()) : "")
                .logo(src.getLogo())
        ;
    }
}
