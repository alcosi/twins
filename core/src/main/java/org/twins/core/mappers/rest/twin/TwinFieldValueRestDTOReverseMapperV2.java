package org.twins.core.mappers.rest.twin;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.cambium.common.exception.ErrorCodeCommon;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.FeaturerService;
import org.springframework.stereotype.Component;
import org.twins.core.dao.datalist.DataListOptionEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.dto.rest.datalist.DataListOptionDTOv1;
import org.twins.core.dto.rest.twin.*;
import org.twins.core.featurer.fieldtyper.FieldTyper;
import org.twins.core.featurer.fieldtyper.FieldTyperList;
import org.twins.core.featurer.fieldtyper.value.*;
import org.twins.core.mappers.rest.MapperProperties;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.datalist.DataListOptionRestDTOMapper;
import org.twins.core.service.twinclass.TwinClassFieldService;

import java.util.UUID;


@Component
@RequiredArgsConstructor
public class TwinFieldValueRestDTOReverseMapperV2 extends RestSimpleDTOMapper<TwinFieldValueDTOv2, FieldValue> {
    final DataListOptionRestDTOMapper dataListOptionRestDTOMapper;
    final TwinClassFieldService twinClassFieldService;
    final FeaturerService featurerService;

    @Override
    public void map(TwinFieldValueDTOv2 src, FieldValue dst, MapperProperties mapperProperties) throws Exception {
        throw new ServiceException(ErrorCodeCommon.NOT_IMPLEMENTED);
    }

    @Override
    public FieldValue convert(TwinFieldValueDTOv2 fieldValueDTO, MapperProperties mapperProperties) throws Exception {
        TwinClassFieldEntity twinClassFieldEntity = twinClassFieldService.findByTwinClassIdAndKey(fieldValueDTO.twinClassId, fieldValueDTO.key);
        FieldTyper fieldTyper = featurerService.getFeaturer(twinClassFieldEntity.getFieldTyperFeaturer(), FieldTyper.class);
        FieldValue fieldValue = null;
        if (fieldTyper.getValueType() == FieldValueText.class)
            fieldValue = new FieldValueText()
                    .value(fieldValueDTO.value);
        if (fieldTyper.getValueType() == FieldValueColorHEX.class)
            fieldValue = new FieldValueColorHEX()
                    .hex(fieldValueDTO.value);
        if (fieldTyper.getValueType() == FieldValueDate.class)
            fieldValue = new FieldValueDate()
                    .date(fieldValueDTO.value);
        if (fieldTyper.getValueType() == FieldValueSelect.class) {
            fieldValue = new FieldValueSelect();
            for (String dataListOptionId : fieldValueDTO.value.split(FieldTyperList.LIST_SPLITTER)) {
                ((FieldValueSelect) fieldValue).add(new DataListOptionEntity()
                        .id(UUID.fromString(dataListOptionId)));
            }
        }
        fieldValue.setTwinClassField(twinClassFieldService.findByTwinClassIdAndKey(fieldValueDTO.twinClassId, fieldValueDTO.key));
        return fieldValue;
    }
}
