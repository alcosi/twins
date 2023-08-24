package org.twins.core.mappers.rest.twin;

import lombok.RequiredArgsConstructor;
import org.cambium.i18n.service.I18nService;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinStatusEntity;
import org.twins.core.dto.rest.twin.TwinStatusDTOv1;
import org.twins.core.mappers.rest.RestDTOMapper;


@Component
@RequiredArgsConstructor
public class TwinStatusRestDTOMapper implements RestDTOMapper<TwinStatusEntity, TwinStatusDTOv1> {
    final I18nService i18nService;

    public TwinStatusDTOv1 convert(TwinStatusEntity twinStatusEntity) {
        TwinStatusDTOv1 twinStatusDTOv1 = new TwinStatusDTOv1();
        map(twinStatusEntity, twinStatusDTOv1);
        return twinStatusDTOv1;
    }

    @Override
    public void map(TwinStatusEntity src, TwinStatusDTOv1 dst) {
        dst
                .id(src.getId())
                .name(i18nService.translateToLocale(src.getNameI18n()))
                .description(src.getDescriptionI18n() != null ? i18nService.translateToLocale(src.getDescriptionI18n()) : "")
                .logo(src.getLogo())
        ;
    }
}
