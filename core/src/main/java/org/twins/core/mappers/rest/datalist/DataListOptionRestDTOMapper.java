package org.twins.core.mappers.rest.datalist;

import lombok.RequiredArgsConstructor;
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

import java.util.Hashtable;


@Component
@RequiredArgsConstructor
@MapperModeBinding(modes = DataListOptionMode.class)
public class DataListOptionRestDTOMapper extends RestSimpleDTOMapper<DataListOptionEntity, DataListOptionDTOv1> {
    @MapperModePointerBinding(modes = DataListMode.DataListOption2DataListMode.class)
    private final DataListRestDTOMapper dataListRestDTOMapper;
    @MapperModePointerBinding(modes = BusinessAccountMode.DataListOption2BusinessAccountMode.class)
    private final BusinessAccountDTOMapper businessAccountDTOMapper;

    @Override
    public void map(DataListOptionEntity src, DataListOptionDTOv1 dst, MapperContext mapperContext) {
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
                        .setCustom(src.isCustom());
            case SHORT ->
                dst
                        .setId(src.getId())
                        .setName(src.getOptionI18nId() != null ? I18nCacheHolder.addId(src.getOptionI18nId()) : src.getOption())
                        .setDescription(I18nCacheHolder.addId(src.getDescriptionI18nId()));
        }
        if (mapperContext.hasModeButNot(DataListMode.DataListOption2DataListMode.HIDE)) {
            dst.setDataListId(src.getDataListId());
            dataListRestDTOMapper.postpone(src.getDataList(), mapperContext.forkOnPoint(mapperContext.getModeOrUse(DataListMode.DataListOption2DataListMode.SHORT)));
        } if (mapperContext.hasModeButNot(BusinessAccountMode.DataListOption2BusinessAccountMode.HIDE)) {
            dst.setBusinessAccountId(src.getBusinessAccountId());
            businessAccountDTOMapper.postpone(src.getBusinessAccount(), mapperContext.forkOnPoint(mapperContext.getModeOrUse(BusinessAccountMode.DataListOption2BusinessAccountMode.SHORT)));
        }
    }

    protected Hashtable<String, String> getAttributes(DataListOptionEntity src) {
        Hashtable<String, String> ret = new Hashtable<>();
        if (src.getAttribute1value() != null && src.getDataList().getAttribute1key() != null)
            ret.put(src.getDataList().getAttribute1key(), src.getAttribute1value());
        if (src.getAttribute2value() != null && src.getDataList().getAttribute2key() != null)
            ret.put(src.getDataList().getAttribute2key(), src.getAttribute2value());
        if (src.getAttribute3value() != null && src.getDataList().getAttribute3key() != null)
            ret.put(src.getDataList().getAttribute3key(), src.getAttribute3value());
        if (src.getAttribute4value() != null && src.getDataList().getAttribute4key() != null)
            ret.put(src.getDataList().getAttribute4key(), src.getAttribute4value());
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
