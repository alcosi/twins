package org.twins.core.mappers.rest.twin;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.cambium.common.exception.ErrorCodeCommon;
import org.cambium.common.exception.ServiceException;
import org.springframework.stereotype.Component;
import org.twins.core.dao.datalist.DataListOptionEntity;
import org.twins.core.dto.rest.datalist.DataListOptionDTOv1;
import org.twins.core.dto.rest.twin.*;
import org.twins.core.featurer.fieldtyper.value.*;
import org.twins.core.mappers.rest.MapperContext;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.datalist.DataListOptionRestDTOMapper;
import org.twins.core.service.twinclass.TwinClassFieldService;


@Component
@RequiredArgsConstructor
public class TwinFieldValueRestDTOReverseMapper extends RestSimpleDTOMapper<TwinFieldValueDTO, FieldValue> {
    final DataListOptionRestDTOMapper dataListOptionRestDTOMapper;
    final TwinClassFieldService twinClassFieldService;

    @Override
    public void map(TwinFieldValueDTO src, FieldValue dst, MapperContext mapperContext) throws Exception {
        throw new ServiceException(ErrorCodeCommon.NOT_IMPLEMENTED);
    }

    @Override
    public FieldValue convert(TwinFieldValueDTO fieldValueDTO, MapperContext mapperContext) throws Exception {
        FieldValue fieldValue = null;
        if (fieldValueDTO instanceof TwinFieldValueTextDTOv1 text)
            fieldValue =  new FieldValueText()
                    .setValue(text.text());
        if (fieldValueDTO instanceof TwinFieldValueColorHexDTOv1 color)
            fieldValue =  new FieldValueColorHEX()
                    .setHex(color.hex());
        if (fieldValueDTO instanceof TwinFieldValueDateDTOv1 date)
            fieldValue = new FieldValueDate()
                    .setDate(date.date());
        if (fieldValueDTO instanceof TwinFieldValueListDTOv1 select) {
            fieldValue = new FieldValueSelect();
            for (DataListOptionDTOv1 dataListOptionDTO : select.selectedOptions()) {
                ((FieldValueSelect)fieldValue).add(new DataListOptionEntity()
                        .setId(dataListOptionDTO.id())
                        .setOption(dataListOptionDTO.name));
            }
        }
        if (fieldValue != null && fieldValueDTO.twinClassId != null && StringUtils.isNoneBlank(fieldValueDTO.fieldKey))
            fieldValue.setTwinClassField(twinClassFieldService.findByTwinClassIdAndKeyIncludeParent(fieldValueDTO.twinClassId, fieldValueDTO.fieldKey));
        return fieldValue;
    }
}
