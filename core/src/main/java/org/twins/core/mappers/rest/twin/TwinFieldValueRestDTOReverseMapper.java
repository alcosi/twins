package org.twins.core.mappers.rest.twin;

import lombok.RequiredArgsConstructor;
import org.cambium.common.exception.ErrorCodeCommon;
import org.cambium.common.exception.ServiceException;
import org.springframework.stereotype.Component;
import org.twins.core.dao.datalist.DataListOptionEntity;
import org.twins.core.dto.rest.datalist.DataListOptionDTOv1;
import org.twins.core.dto.rest.twin.*;
import org.twins.core.featurer.fieldtyper.*;
import org.twins.core.mappers.rest.MapperProperties;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.datalist.DataListOptionRestDTOMapper;


@Component
@RequiredArgsConstructor
public class TwinFieldValueRestDTOReverseMapper extends RestSimpleDTOMapper<TwinFieldValueDTO, FieldValue> {
    final DataListOptionRestDTOMapper dataListOptionRestDTOMapper;

    @Override
    public void map(TwinFieldValueDTO src, FieldValue dst, MapperProperties mapperProperties) throws Exception {
        throw new ServiceException(ErrorCodeCommon.NOT_IMPLEMENTED);
    }

    @Override
    public FieldValue convert(TwinFieldValueDTO fieldValueDTO) throws Exception {
        if (fieldValueDTO instanceof TwinFieldValueTextDTOv1 text)
            return new FieldValueText()
                    .value(text.text());
        if (fieldValueDTO instanceof TwinFieldValueColorHexDTOv1 color)
            return new FieldValueColorHEX()
                    .hex(color.hex());
        if (fieldValueDTO instanceof TwinFieldValueDateDTOv1 date)
            return new FieldValueDate()
                    .date(date.date());
        if (fieldValueDTO instanceof TwinFieldValueDataListOptionsDTOv1 select) {
            FieldValueSelect fieldValueSelect = new FieldValueSelect();
            for (DataListOptionDTOv1 dataListOptionDTO : select.selectedOptions())
                fieldValueSelect.add(new DataListOptionEntity()
                        .id(dataListOptionDTO.id())
                        .option(dataListOptionDTO.name));
        }
        return null;
    }
}
