package org.twins.core.mappers.rest.datalist;

import lombok.RequiredArgsConstructor;
import org.cambium.i18n.service.I18nService;
import org.springframework.stereotype.Component;
import org.twins.core.dao.datalist.DataListOptionEntity;
import org.twins.core.dto.rest.datalist.DataListOptionDTOv1;
import org.twins.core.mappers.rest.MapperMode;
import org.twins.core.mappers.rest.MapperProperties;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;

import java.util.Hashtable;


@Component
@RequiredArgsConstructor
public class DataListOptionRestDTOMapper extends RestSimpleDTOMapper<DataListOptionEntity, DataListOptionDTOv1> {
    final I18nService i18nService;

    @Override
    public void map(DataListOptionEntity src, DataListOptionDTOv1 dst, MapperProperties mapperProperties) {
        switch (mapperProperties.getModeOrUse(Mode.DETAILED)) {
            case DETAILED:
                dst
                        .icon(src.getIcon())
                        .attributes(getAttributes(src))
                        .disabled(src.isDisabled());
            case ID_NAME_ONLY:
                dst
                        .id(src.getId())
                        .name(src.getOptionI18n() != null ? i18nService.translateToLocale(src.getOptionI18n()) : src.getOption());
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

    public enum Mode implements MapperMode {
        ID_NAME_ONLY, DETAILED;

        public static final String _ID_NAME_ONLY = "ID_NAME_ONLY";
        public static final String _DETAILED = "DETAILED";
    }
}
