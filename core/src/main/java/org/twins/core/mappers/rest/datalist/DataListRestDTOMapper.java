package org.twins.core.mappers.rest.datalist;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.cambium.i18n.service.I18nService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModeBinding;
import org.twins.core.dao.datalist.DataListEntity;
import org.twins.core.domain.datalist.DataListAttribute;
import org.twins.core.dto.rest.datalist.DataListDTOv1;
import org.twins.core.mappers.rest.mappercontext.modes.DataListMode;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;


@Component
@RequiredArgsConstructor
@MapperModeBinding(modes = DataListMode.class)
public class DataListRestDTOMapper extends RestSimpleDTOMapper<DataListEntity, DataListDTOv1> {

    private final I18nService i18nService;
    private final DataListAttributeRestDTOMapper dataListAttributeRestDTOMapper;

    @Override
    public void map(DataListEntity src, DataListDTOv1 dst, MapperContext mapperContext) throws Exception {
        switch (mapperContext.getModeOrUse(DataListMode.DETAILED)) {
            case MANAGED:
                dst
                        .setId(src.getId())
                        .setName(i18nService.translateToLocale(src.getNameI18nId()))
                        .setDescription(i18nService.translateToLocale(src.getDescriptionI18NId()))
                        .setKey(src.getKey())
                        .setAttribute1(dataListAttributeRestDTOMapper.convert(new ImmutablePair<>(src.getAttribute1key(), src.getAttribute1nameI18nId())))
                        .setAttribute2(dataListAttributeRestDTOMapper.convert(new ImmutablePair<>(src.getAttribute1key(), src.getAttribute1nameI18nId())))
                        .setAttribute3(dataListAttributeRestDTOMapper.convert(new ImmutablePair<>(src.getAttribute1key(), src.getAttribute1nameI18nId())))
                        .setAttribute4(dataListAttributeRestDTOMapper.convert(new ImmutablePair<>(src.getAttribute1key(), src.getAttribute1nameI18nId())))
                        .setUpdatedAt(src.getUpdatedAt().toLocalDateTime());
            case DETAILED:
                dst
                        .setId(src.getId())
                        .setName(i18nService.translateToLocale(src.getNameI18nId()))
                        .setDescription(i18nService.translateToLocale(src.getDescriptionI18NId()))
                        .setKey(src.getKey())
                        .setUpdatedAt(src.getUpdatedAt().toLocalDateTime());
                break;
            case SHORT:
                dst
                        .setId(src.getId())
                        .setName(i18nService.translateToLocale(src.getNameI18nId()));
                break;
        }
    }
}
