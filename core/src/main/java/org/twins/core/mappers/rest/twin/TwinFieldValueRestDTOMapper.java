package org.twins.core.mappers.rest.twin;

import lombok.RequiredArgsConstructor;
import org.cambium.featurer.FeaturerService;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinFieldEntity;
import org.twins.core.dto.rest.datalist.DataListOptionDTOv1;
import org.twins.core.dto.rest.twin.*;
import org.twins.core.featurer.fieldtyper.*;
import org.twins.core.mappers.rest.MapperMode;
import org.twins.core.mappers.rest.MapperProperties;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.datalist.DataListOptionRestDTOMapper;
import org.twins.core.mappers.rest.twinclass.TwinClassFieldRestDTOMapper;
import org.twins.core.mappers.rest.twinclass.TwinClassRestDTOMapper;
import org.twins.core.mappers.rest.user.UserDTOMapper;

import java.util.Map;
import java.util.UUID;


@Component
@RequiredArgsConstructor
public class TwinFieldValueRestDTOMapper extends RestSimpleDTOMapper<TwinFieldEntity, TwinFieldValueDTOv1> {
    final UserDTOMapper userDTOMapper;
    final TwinClassFieldRestDTOMapper twinClassFieldRestDTOMapper;
    final TwinClassRestDTOMapper twinClassRestDTOMapper;
    final FeaturerService featurerService;
    final DataListOptionRestDTOMapper dataListOptionRestDTOMapper;

    @Override
    public void map(TwinFieldEntity src, TwinFieldValueDTOv1 dst, MapperProperties mapperProperties) throws Exception {
        switch (mapperProperties.getModeOrUse(Mode.FIELDS_KEY_VALUE_ONLY)) {
            case FIELDS_TYPE_DETAILED:
                twinClassFieldRestDTOMapper.map(src.twinClassField(), dst, mapperProperties);
            case FIELDS_KEY_VALUE_ONLY:
                dst.key(src.twinClassField().key());
                FieldTyper fieldTyper = featurerService.getFeaturer(src.twinClassField().fieldTyperFeaturer(), FieldTyper.class);
                dst.value(convert(fieldTyper.deserializeValue(src.twinClassField().fieldTyperParams(), src.value())));
        }
    }

    public TwinFieldValue convert(FieldValue fieldValue) throws Exception {
        if (fieldValue instanceof FieldValueText text)
            return new TwinFieldValueText()
                    .text(text.value());
        if (fieldValue instanceof FieldValueColorHEX color)
            return new TwinFieldValueColorHex()
                    .hex(color.hex());
        if (fieldValue instanceof FieldValueDate date)
            return new TwinFieldValueDate()
                    .date(date.date());
        if (fieldValue instanceof FieldValueSelect select)
            return new TwinFieldValueDataListOptions()
                    .selectedOptions(dataListOptionRestDTOMapper.convertList(select.options(), new MapperProperties().setMode(DataListOptionRestDTOMapper.Mode.ID_NAME_ONLY)));
        return null;
    }

    public enum Mode implements MapperMode {
        FIELDS_KEY_VALUE_ONLY, FIELDS_TYPE_DETAILED;

        public static final String _FIELDS_KEY_VALUE_ONLY = "FIELDS_KEY_VALUE_ONLY";
        public static final String _FIELDS_TYPE_DETAILED = "FIELDS_TYPE_DETAILED";

    }
}
