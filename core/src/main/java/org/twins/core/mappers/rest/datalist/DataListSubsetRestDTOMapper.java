package org.twins.core.mappers.rest.datalist;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModeBinding;
import org.twins.core.controller.rest.annotation.MapperModePointerBinding;
import org.twins.core.dao.datalist.DataListSubsetEntity;
import org.twins.core.dto.rest.datalist.DataListSubsetDTOv1;
import org.twins.core.holder.I18nCacheHolder;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.DataListMode;
import org.twins.core.mappers.rest.mappercontext.modes.DataListSubsetMode;
import org.twins.core.mappers.rest.mappercontext.modes.UserMode;
import org.twins.core.mappers.rest.user.UserRestDTOMapper;
import org.twins.core.service.datalist.DataListSubsetService;

import java.util.Collection;

import static org.cambium.common.util.DateUtils.convertOrNull;

@Component
@RequiredArgsConstructor
@MapperModeBinding(modes = DataListSubsetMode.class)
public class DataListSubsetRestDTOMapper extends RestSimpleDTOMapper<DataListSubsetEntity, DataListSubsetDTOv1> {
    @MapperModePointerBinding(modes = DataListMode.DataListSubset2DataListMode.class)
    private final DataListRestDTOMapper dataListRestDTOMapper;

    @MapperModePointerBinding(modes = UserMode.DataListSubset2UserMode.class)
    private final UserRestDTOMapper userRestDTOMapper;

    private final DataListSubsetService dataListSubsetService;


    @Override
    public void map(DataListSubsetEntity src, DataListSubsetDTOv1 dst, MapperContext mapperContext) throws Exception {
        if (src == null) {
            return;
        }
        switch (mapperContext.getModeOrUse(DataListSubsetMode.DETAILED)) {
            case MANAGED ->
                    dst
                            .setId(src.getId())
                            .setDataListId(src.getDataListId())
                            .setName(I18nCacheHolder.addId(src.getNameI18nId()))
                            .setDescription(I18nCacheHolder.addId(src.getDescriptionI18NId()))
                            .setKey(src.getKey())
                            .setCreatedAt(convertOrNull(src.getCreatedAt()))
                            .setCreatedByUserId(src.getCreatedByUserId());
            case DETAILED ->
                    dst
                            .setId(src.getId())
                            .setDataListId(src.getDataListId())
                            .setName(I18nCacheHolder.addId(src.getNameI18nId()))
                            .setDescription(I18nCacheHolder.addId(src.getDescriptionI18NId()))
                            .setKey(src.getKey());
            case SHORT ->
                    dst
                            .setId(src.getId())
                            .setDataListId(src.getDataListId())
                            .setKey(src.getKey());
        }
        if (mapperContext.hasModeButNot(DataListMode.DataListSubset2DataListMode.HIDE)) {
            dst.setDataListId(src.getDataListId());
            dataListSubsetService.loadDataList(src);
            dataListRestDTOMapper.postpone(src.getDataList(), mapperContext.forkOnPoint(mapperContext.getModeOrUse(DataListMode.DataListOption2DataListMode.SHORT)));
        }
        if (mapperContext.hasModeButNot(UserMode.DataListSubset2UserMode.HIDE)) {
            dst.setCreatedByUserId(src.getCreatedByUserId());
            dataListSubsetService.loadUser(src);
            userRestDTOMapper.postpone(src.getCreatedByUser(), mapperContext.forkOnPoint(mapperContext.getModeOrUse(UserMode.DataListSubset2UserMode.SHORT)));
        }
    }

    @Override
    public void beforeCollectionConversion(Collection<DataListSubsetEntity> srcCollection, MapperContext mapperContext) throws Exception {
        if (mapperContext.hasModeButNot(DataListMode.DataListSubset2DataListMode.HIDE)) {
            dataListSubsetService.loadDataList(srcCollection);
        }
        if (mapperContext.hasModeButNot(UserMode.DataListSubset2UserMode.HIDE)) {
            dataListSubsetService.loadUser(srcCollection);
        }
    }
}
