package org.twins.core.mappers.rest.datalist;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModeBinding;
import org.twins.core.dao.datalist.DataListEntity;
import org.twins.core.dto.rest.datalist.DataListDTOv1;
import org.twins.core.holder.I18nCacheHolder;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.DataListMode;
import org.twins.core.service.i18n.I18nService;

import static org.cambium.common.util.DateUtils.convertOrNull;


@Component
@RequiredArgsConstructor
@MapperModeBinding(modes = DataListMode.class)
public class DataListRestDTOMapper extends RestSimpleDTOMapper<DataListEntity, DataListDTOv1> {

    private final I18nService i18nService;
    private final DataListAttributeRestDTOMapper dataListAttributeRestDTOMapper;

    @Override
    public void map(DataListEntity src, DataListDTOv1 dst, MapperContext mapperContext) throws Exception {
        switch (mapperContext.getModeOrUse(DataListMode.DETAILED)) {
            case MANAGED -> {
                dst
                        .setId(src.getId())
                        .setName(I18nCacheHolder.addId(src.getNameI18nId()))
                        .setDescription(I18nCacheHolder.addId(src.getDescriptionI18NId()))
                        .setKey(src.getKey())
                        .setCreatedAt(src.getCreatedAt().toLocalDateTime())
                        .setUpdatedAt(convertOrNull(src.getUpdatedAt()))
                        .setExternalId(src.getExternalId());
                if (StringUtils.isNotBlank(src.getAttribute1key()))
                    dst.setAttribute1(dataListAttributeRestDTOMapper.convert(new ImmutablePair<>(src.getAttribute1key(), src.getAttribute1nameI18nId())));
                if (StringUtils.isNotBlank(src.getAttribute2key()))
                    dst.setAttribute2(dataListAttributeRestDTOMapper.convert(new ImmutablePair<>(src.getAttribute2key(), src.getAttribute2nameI18nId())));
                if (StringUtils.isNotBlank(src.getAttribute3key()))
                    dst.setAttribute3(dataListAttributeRestDTOMapper.convert(new ImmutablePair<>(src.getAttribute3key(), src.getAttribute3nameI18nId())));
                if (StringUtils.isNotBlank(src.getAttribute4key()))
                    dst.setAttribute4(dataListAttributeRestDTOMapper.convert(new ImmutablePair<>(src.getAttribute4key(), src.getAttribute4nameI18nId())));
            }
            case DETAILED ->
                dst
                        .setId(src.getId())
                        .setName(I18nCacheHolder.addId(src.getNameI18nId()))
                        .setDescription(I18nCacheHolder.addId(src.getDescriptionI18NId()))
                        .setKey(src.getKey())
                        .setCreatedAt(src.getCreatedAt().toLocalDateTime())
                        .setUpdatedAt(convertOrNull(src.getUpdatedAt()))
                        .setExternalId(src.getExternalId());
            case SHORT ->
                dst
                        .setId(src.getId())
                        .setName(I18nCacheHolder.addId(src.getNameI18nId()));
        }
    }
    
    @Override
    public String getObjectCacheId(DataListEntity src) {
        return src.getId().toString();
    }
}
