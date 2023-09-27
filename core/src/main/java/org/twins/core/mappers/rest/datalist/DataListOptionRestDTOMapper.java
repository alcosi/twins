package org.twins.core.mappers.rest.datalist;

import lombok.RequiredArgsConstructor;
import org.cambium.i18n.service.I18nService;
import org.springframework.stereotype.Component;
import org.twins.core.dao.datalist.DataListOptionEntity;
import org.twins.core.dto.rest.datalist.DataListOptionDTOv1;
import org.twins.core.mappers.rest.MapperMode;
import org.twins.core.mappers.rest.MapperProperties;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;


@Component
@RequiredArgsConstructor
public class DataListOptionRestDTOMapper extends RestSimpleDTOMapper<DataListOptionEntity, DataListOptionDTOv1> {
    final I18nService i18nService;

    @Override
    public void map(DataListOptionEntity entity, DataListOptionDTOv1 dto, MapperProperties mapperProperties) {
        switch (mapperProperties.getModeOrUse(Mode.DETAILED)) {
            case DETAILED:
                dto.disabled(entity.isDisabled());
            case ID_NAME_ONLY:
                dto
                        .id(entity.getId())
                        .name(entity.getOptionI18n() != null ? i18nService.translateToLocale(entity.getOptionI18n()) : entity.getOption());
        }
    }

    public enum Mode implements MapperMode {
        ID_NAME_ONLY, DETAILED;

        public static final String _ID_NAME_ONLY = "ID_NAME_ONLY";
        public static final String _DETAILED = "DETAILED";
    }
}
