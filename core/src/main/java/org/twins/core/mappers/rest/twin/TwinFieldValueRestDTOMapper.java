package org.twins.core.mappers.rest.twin;

import lombok.RequiredArgsConstructor;
import org.cambium.common.exception.ErrorCodeCommon;
import org.cambium.common.exception.ServiceException;
import org.springframework.stereotype.Component;
import org.twins.core.dto.rest.twin.*;
import org.twins.core.featurer.fieldtyper.value.*;
import org.twins.core.mappers.rest.MapperContext;
import org.twins.core.mappers.rest.MapperMode;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.datalist.DataListOptionRestDTOMapper;


@Component
@RequiredArgsConstructor
public class TwinFieldValueRestDTOMapper extends RestSimpleDTOMapper<FieldValue, TwinFieldValueDTO> {


    private final DataListOptionRestDTOMapper dataListOptionRestDTOMapper;

    @Override
    public void map(FieldValue src, TwinFieldValueDTO dst, MapperContext mapperContext) throws Exception {
        throw new ServiceException(ErrorCodeCommon.NOT_IMPLEMENTED);
    }

    @Override
    public TwinFieldValueDTO convert(FieldValue fieldValue, MapperContext mapperContext) throws Exception {
        if (fieldValue instanceof FieldValueText text)
            return new TwinFieldValueTextDTOv1()
                    .text(text.getValue());
        if (fieldValue instanceof FieldValueColorHEX color)
            return new TwinFieldValueColorHexDTOv1()
                    .hex(color.getHex());
        if (fieldValue instanceof FieldValueDate date)
            return new TwinFieldValueDateDTOv1()
                    .date(date.getDate());
        if (fieldValue instanceof FieldValueSelect select)
            return new TwinFieldValueListDTOv1()
                    .selectedOptions(dataListOptionRestDTOMapper.convertCollection(select.getOptions(), new MapperContext().setMode(MapperMode.DataListOptionMode.SHORT)));
        return null;
    }
}
