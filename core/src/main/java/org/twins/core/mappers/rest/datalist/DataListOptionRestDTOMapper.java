package org.twins.core.mappers.rest.datalist;

import lombok.RequiredArgsConstructor;
import org.cambium.common.exception.ServiceException;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModeBinding;
import org.twins.core.controller.rest.annotation.MapperModePointerBinding;
import org.twins.core.dao.datalist.DataListOptionEntity;
import org.twins.core.dto.rest.datalist.DataListOptionDTOv1;
import org.twins.core.holder.I18nCacheHolder;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.businessaccount.BusinessAccountDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.BusinessAccountMode;
import org.twins.core.mappers.rest.mappercontext.modes.DataListMode;
import org.twins.core.mappers.rest.mappercontext.modes.DataListOptionMode;
import org.twins.core.service.datalist.DataListOptionService;

import java.util.Collection;
import java.util.Hashtable;

import static org.cambium.common.util.DateUtils.convertOrNull;

@Component
@RequiredArgsConstructor
@MapperModeBinding(modes = DataListOptionMode.class)
public class DataListOptionRestDTOMapper extends RestSimpleDTOMapper<DataListOptionEntity, DataListOptionDTOv1> {
    @MapperModePointerBinding(modes = DataListMode.DataListOption2DataListMode.class)
    private final DataListRestDTOMapper dataListRestDTOMapper;
    @MapperModePointerBinding(modes = BusinessAccountMode.DataListOption2BusinessAccountMode.class)
    private final BusinessAccountDTOMapper businessAccountDTOMapper;
    private final DataListOptionService dataListOptionService;

    @Override
    public void map(DataListOptionEntity src, DataListOptionDTOv1 dst, MapperContext mapperContext) throws Exception {
        switch (mapperContext.getModeOrUse(DataListOptionMode.DETAILED)) {
            case DETAILED ->
                dst
                        .setId(src.getId())
                        .setName(src.getOptionI18nId() != null ? I18nCacheHolder.addId(src.getOptionI18nId()) : src.getOption())
                        .setDescription(I18nCacheHolder.addId(src.getDescriptionI18nId()))
                        .setIcon(src.getIcon())
                        .setAttributes(getAttributes(src))
                        .setStatus(src.getStatus())
                        .setBackgroundColor(src.getBackgroundColor())
                        .setExternalId(src.getExternalId())
                        .setFontColor(src.getFontColor())
                        .setDataListId(src.getDataListId())
                        .setBusinessAccountId(src.getBusinessAccountId())
                        .setCustom(src.isCustom())
                        .setCreatedAt(convertOrNull(src.getCreatedAt()));
            case SHORT ->
                dst
                        .setId(src.getId())
                        .setName(src.getOptionI18nId() != null ? I18nCacheHolder.addId(src.getOptionI18nId()) : src.getOption())
                        .setDescription(I18nCacheHolder.addId(src.getDescriptionI18nId()));
        }
        if (mapperContext.hasModeButNot(DataListMode.DataListOption2DataListMode.HIDE)) {
            dst.setDataListId(src.getDataListId());
            dataListOptionService.loadDataList(src);
            dataListRestDTOMapper.postpone(src.getDataList(), mapperContext.forkOnPoint(mapperContext.getModeOrUse(DataListMode.DataListOption2DataListMode.SHORT)));
        } if (src.getBusinessAccountId() != null && mapperContext.hasModeButNot(BusinessAccountMode.DataListOption2BusinessAccountMode.HIDE)) {
            dst.setBusinessAccountId(src.getBusinessAccountId());
            dataListOptionService.loadBusinessAccount(src);
            businessAccountDTOMapper.postpone(src.getBusinessAccount(), mapperContext.forkOnPoint(mapperContext.getModeOrUse(BusinessAccountMode.DataListOption2BusinessAccountMode.SHORT)));
        }
    }

    @Override
    public void beforeCollectionConversion(Collection<DataListOptionEntity> srcCollection, MapperContext mapperContext) throws Exception {
        if (mapperContext.hasMode(DataListOptionMode.DETAILED) || mapperContext.hasModeButNot(DataListMode.DataListOption2DataListMode.HIDE)) {
            dataListOptionService.loadDataList(srcCollection);
        }
        if (mapperContext.hasModeButNot(BusinessAccountMode.DataListOption2BusinessAccountMode.HIDE)) {
            dataListOptionService.loadBusinessAccount(srcCollection);
        }
    }

    protected Hashtable<String, String> getAttributes(DataListOptionEntity src) throws ServiceException {
        Hashtable<String, String> ret = new Hashtable<>();
        dataListOptionService.loadDataList(src);
        var dataList = src.getDataList();
        if (src.getAttribute1value() != null && dataList.getAttribute1key() != null)
            ret.put(dataList.getAttribute1key(), src.getAttribute1value());
        if (src.getAttribute2value() != null && dataList.getAttribute2key() != null)
            ret.put(dataList.getAttribute2key(), src.getAttribute2value());
        if (src.getAttribute3value() != null && dataList.getAttribute3key() != null)
            ret.put(dataList.getAttribute3key(), src.getAttribute3value());
        if (src.getAttribute4value() != null && dataList.getAttribute4key() != null)
            ret.put(dataList.getAttribute4key(), src.getAttribute4value());
        return !ret.isEmpty() ? ret : null;
    }

    @Override
    public boolean hideMode(MapperContext mapperContext) {
        return mapperContext.hasModeOrEmpty(DataListOptionMode.HIDE);
    }

    @Override
    public String getObjectCacheId(DataListOptionEntity src) {
        return src.getId().toString();
    }
}
