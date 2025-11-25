package org.twins.core.mappers.rest.twinclass;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModeBinding;
import org.twins.core.dao.twinclass.TwinClassOwnerTypeEntity;
import org.twins.core.dto.rest.twinclass.TwinClassOwnerTypeDTOv1;
import org.twins.core.enums.twinclass.OwnerType;
import org.twins.core.holder.I18nCacheHolder;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.TwinClassMode;
import org.twins.core.service.i18n.I18nService;


@Component
@RequiredArgsConstructor
@MapperModeBinding(modes = TwinClassMode.class)
public class TwinClassOwnerTypeRestDTOMapper extends RestSimpleDTOMapper<TwinClassOwnerTypeEntity, TwinClassOwnerTypeDTOv1> {

    private final I18nService i18nService;

    @Override
    public void map(TwinClassOwnerTypeEntity src, TwinClassOwnerTypeDTOv1 dst, MapperContext mapperContext) throws Exception {
        dst
                .setId(OwnerType.valueOd(src.getId()))
                .setName(I18nCacheHolder.addId(src.getNameI18nId()))
                .setDescription(I18nCacheHolder.addId(src.getDescriptionI18nId()));
    }
}
