package org.twins.core.mappers.rest.twinclass;

import lombok.RequiredArgsConstructor;
import org.cambium.i18n.service.I18nService;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.dto.rest.twinclass.TwinClassDTOv1;
import org.twins.core.dto.rest.twinclass.TwinClassFieldDTOv1;
import org.twins.core.mappers.rest.RestDTOMapper;


@Component
@RequiredArgsConstructor
public class TwinClassFieldRestDTOMapper implements RestDTOMapper<TwinClassFieldEntity, TwinClassFieldDTOv1> {
    final I18nService i18nService;

    public TwinClassFieldDTOv1 convert(TwinClassFieldEntity twinClassFieldEntity) {
        TwinClassFieldDTOv1 twinClassFieldDTOv1 = new TwinClassFieldDTOv1();
        map(twinClassFieldEntity, twinClassFieldDTOv1);
        return twinClassFieldDTOv1;
    }

    @Override
    public void map(TwinClassFieldEntity src, TwinClassFieldDTOv1 dst) {
        dst
                .id(src.getId())
                .key(src.getKey())
                .name(i18nService.translateToLocale(src.getNameI18n()))
                .description(src.getDescriptionI18n() != null ? i18nService.translateToLocale(src.getDescriptionI18n()) : "")
        ;
    }
}
