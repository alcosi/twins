package org.twins.core.mappers.rest.datalist;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModeBinding;
import org.twins.core.dao.datalist.DataListOptionEntity;
import org.twins.core.dto.rest.datalist.DataListOptionDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.DataListOptionMode;
import org.twins.core.service.i18n.I18nService;

import java.util.Hashtable;


@Component
@RequiredArgsConstructor
@MapperModeBinding(modes = DataListOptionMode.class)
public class DataListOptionRestDTOMapper extends RestSimpleDTOMapper<DataListOptionEntity, DataListOptionDTOv1> {

    private final I18nService i18nService;

    @Override
    public void map(DataListOptionEntity src, DataListOptionDTOv1 dst, MapperContext mapperContext) {
        switch (mapperContext.getModeOrUse(DataListOptionMode.DETAILED)) {
            case DETAILED ->
                dst
                        .setId(src.getId())
                        .setName(src.getOptionI18NId() != null ? i18nService.translateToLocale(src.getOptionI18NId()) : src.getOption())
                        .setIcon(src.getIcon())
                        .setAttributes(getAttributes(src))
                        .setStatus(src.getStatus())
                        .setBackgroundColor(src.getBackgroundColor())
                        .setExternalId(src.getExternalId())
                        .setFontColor(src.getFontColor());
            case SHORT ->
                dst
                        .setId(src.getId())
                        .setName(src.getOptionI18NId() != null ? i18nService.translateToLocale(src.getOptionI18NId()) : src.getOption());
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
