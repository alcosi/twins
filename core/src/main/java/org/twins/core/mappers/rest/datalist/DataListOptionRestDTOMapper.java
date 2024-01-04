package org.twins.core.mappers.rest.datalist;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.cambium.i18n.service.I18nService;
import org.springframework.stereotype.Component;
import org.twins.core.dao.datalist.DataListOptionEntity;
import org.twins.core.dto.rest.datalist.DataListOptionDTOv1;
import org.twins.core.mappers.rest.MapperContext;
import org.twins.core.mappers.rest.MapperMode;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;

import java.util.Hashtable;


@Component
@RequiredArgsConstructor
public class DataListOptionRestDTOMapper extends RestSimpleDTOMapper<DataListOptionEntity, DataListOptionDTOv1> {
    final I18nService i18nService;

    @Override
    public void map(DataListOptionEntity src, DataListOptionDTOv1 dst, MapperContext mapperContext) {
        switch (mapperContext.getModeOrUse(Mode.DETAILED)) {
            case DETAILED:
                dst
                        .id(src.getId())
                        .name(src.getOptionI18NId() != null ? i18nService.translateToLocale(src.getOptionI18NId()) : src.getOption())
                        .icon(src.getIcon())
                        .attributes(getAttributes(src))
                        .disabled(src.isDisabled());
                break;
            case SHORT:
                dst
                        .id(src.getId())
                        .name(src.getOptionI18NId() != null ? i18nService.translateToLocale(src.getOptionI18NId()) : src.getOption());
                break;
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
        return ret.size() > 0 ? ret : null;
    }

    @AllArgsConstructor
    public enum Mode implements MapperMode {
        HIDE(0),
        SHORT(1),
        DETAILED(2);

        public static final String _HIDE = "HIDE";
        public static final String _SHORT = "SHORT";
        public static final String _DETAILED = "DETAILED";

        @Getter
        final int priority;
    }

    @Override
    public boolean hideMode(MapperContext mapperContext) {
        return mapperContext.hasModeOrEmpty(Mode.HIDE);
    }

    @Override
    public String getObjectCacheId(DataListOptionEntity src) {
        return src.getId().toString();
    }
}
