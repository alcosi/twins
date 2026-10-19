package org.twins.core.mappers.rest.datalist;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModeBinding;
import org.twins.core.controller.rest.annotation.MapperModePointerBinding;
import org.twins.core.dao.datalist.DataListEntity;
import org.twins.core.dto.rest.datalist.DataListDTOv1;
import org.twins.core.holder.I18nCacheHolder;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.DataListMode;
import org.twins.core.mappers.rest.mappercontext.modes.UserMode;
import org.twins.core.mappers.rest.user.UserRestDTOMapper;
import org.twins.core.service.datalist.DataListService;

import java.util.Collection;

import static org.cambium.common.util.DateUtils.convertOrNull;


@Component
@RequiredArgsConstructor
@MapperModeBinding(modes = DataListMode.class)
public class DataListRestDTOMapper extends RestSimpleDTOMapper<DataListEntity, DataListDTOv1> {
    @MapperModePointerBinding(modes = UserMode.DataList2UserMode.class)
    private final UserRestDTOMapper userRestDTOMapper;

    private final DataListService dataListService;
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
                        .setCreatedAt(convertOrNull(src.getCreatedAt()))
                        .setCreatedByUserId(src.getCreatedByUserId())
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
                        .setCreatedAt(convertOrNull(src.getCreatedAt()))
                        .setUpdatedAt(convertOrNull(src.getUpdatedAt()))
                        .setExternalId(src.getExternalId());
            case SHORT ->
                dst
                        .setId(src.getId())
                        .setName(I18nCacheHolder.addId(src.getNameI18nId()));
        }
        if (mapperContext.hasModeButNot(UserMode.DataList2UserMode.HIDE)) {
            dst.setCreatedByUserId(src.getCreatedByUserId());
            dataListService.loadUser(src);
            userRestDTOMapper.postpone(src.getCreatedByUser(), mapperContext.forkOnPoint(mapperContext.getModeOrUse(UserMode.DataList2UserMode.SHORT)));
        }
    }
    
    @Override
    public String getObjectCacheId(DataListEntity src) {
        return src.getId().toString();
    }

    @Override
    public void beforeCollectionConversion(Collection<DataListEntity> srcCollection, MapperContext mapperContext) throws Exception {
        if (mapperContext.hasModeButNot(UserMode.DataList2UserMode.HIDE)) {
            dataListService.loadUser(srcCollection);
        }
    }
}
