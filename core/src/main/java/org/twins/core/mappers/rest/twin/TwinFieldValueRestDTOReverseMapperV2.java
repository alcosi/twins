package org.twins.core.mappers.rest.twin;

import lombok.RequiredArgsConstructor;
import org.cambium.common.exception.ErrorCodeCommon;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.FeaturerService;
import org.springframework.stereotype.Component;
import org.twins.core.dao.datalist.DataListOptionEntity;
import org.twins.core.dao.twin.TwinFieldEntity;
import org.twins.core.featurer.fieldtyper.FieldTyper;
import org.twins.core.featurer.fieldtyper.FieldTyperList;
import org.twins.core.featurer.fieldtyper.value.*;
import org.twins.core.mappers.rest.MapperProperties;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.datalist.DataListOptionRestDTOMapper;
import org.twins.core.service.twin.TwinService;
import org.twins.core.service.twinclass.TwinClassFieldService;

import java.util.UUID;


@Component
@RequiredArgsConstructor
public class TwinFieldValueRestDTOReverseMapperV2 extends RestSimpleDTOMapper<FieldValueText, FieldValue> {
    final DataListOptionRestDTOMapper dataListOptionRestDTOMapper;
    final TwinClassFieldService twinClassFieldService;
    final TwinService twinService;
    final FeaturerService featurerService;

    @Override
    public void map(FieldValueText src, FieldValue dst, MapperProperties mapperProperties) throws Exception {
        throw new ServiceException(ErrorCodeCommon.NOT_IMPLEMENTED);
    }

    @Override
    public FieldValue convert(FieldValueText fieldValueText, MapperProperties mapperProperties) throws Exception {
        FieldTyper fieldTyper = featurerService.getFeaturer(fieldValueText.getTwinClassField().getFieldTyperFeaturer(), FieldTyper.class);
        FieldValue fieldValue = null;
        if (fieldTyper.getValueType() == FieldValueText.class)
            fieldValue = fieldValueText;
        if (fieldTyper.getValueType() == FieldValueColorHEX.class)
            fieldValue = new FieldValueColorHEX()
                    .hex(fieldValueText.getValue());
        if (fieldTyper.getValueType() == FieldValueDate.class)
            fieldValue = new FieldValueDate()
                    .date(fieldValueText.getValue());
        if (fieldTyper.getValueType() == FieldValueSelect.class) {
            fieldValue = new FieldValueSelect();
            for (String dataListOptionId : fieldValueText.getValue().split(FieldTyperList.LIST_SPLITTER)) {
                ((FieldValueSelect) fieldValue).add(new DataListOptionEntity()
                        .id(UUID.fromString(dataListOptionId)));
            }
        }
        return fieldValue.setTwinClassField(fieldValueText.getTwinClassField());
    }

    public FieldValueText createValueByClassIdAndFieldKey(UUID twinClassId, String fieldKey, String fieldValue) {
        return (FieldValueText) new FieldValueText()
                .setValue(fieldValue)
                .setTwinClassField(twinClassFieldService.findByTwinClassIdAndKey(twinClassId, fieldKey));
    }

    public FieldValueText createValueByTwinFieldId(UUID twinFieldId, String fieldValue) throws ServiceException {
        TwinFieldEntity twinFieldEntity = twinService.findTwinField(twinFieldId);
        return (FieldValueText) new FieldValueText()
                .setValue(fieldValue)
                .setTwinClassField(twinFieldEntity.twinClassField());
    }

    public FieldValueText createByTwinIdAndFieldKey(UUID twinId, String fieldKey, String fieldValue) throws ServiceException {
        TwinFieldEntity twinFieldEntity = twinService.findTwinFieldIncludeMissing(twinId, fieldKey);
        return (FieldValueText) new FieldValueText()
                .setValue(fieldValue)
                .setTwinClassField(twinFieldEntity.twinClassField());
    }
}
