package org.twins.core.mappers.rest.twinclass;

import lombok.RequiredArgsConstructor;
import org.cambium.i18n.service.I18nService;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModeBinding;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.dao.twinclass.TwinClassOwnerTypeEntity;
import org.twins.core.dto.rest.twinclass.TwinClassOwnerTypeDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.TwinClassMode;


@Component
@RequiredArgsConstructor
@MapperModeBinding(modes = TwinClassMode.class)
public class TwinClassOwnerTypeRestDTOMapper extends RestSimpleDTOMapper<TwinClassOwnerTypeEntity, TwinClassOwnerTypeDTOv1> {

    private final I18nService i18nService;

    @Override
    public void map(TwinClassOwnerTypeEntity src, TwinClassOwnerTypeDTOv1 dst, MapperContext mapperContext) throws Exception {
        dst
                .setId(TwinClassEntity.OwnerType.valueOd(src.getId()))
                .setName(i18nService.translateToLocale(src.getNameI18nId()))
                .setDescription(i18nService.translateToLocale(src.getDescriptionI18nId()));
    }
}
