package org.twins.core.mappers.rest.twin;

import lombok.RequiredArgsConstructor;
import org.cambium.common.exception.ErrorCodeCommon;
import org.cambium.common.exception.ServiceException;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModePointerBinding;
import org.twins.core.dto.rest.twin.*;
import org.twins.core.featurer.fieldtyper.value.*;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.datalist.DataListOptionRestDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.DataListOptionMode;


@Component
@RequiredArgsConstructor
public class TwinFieldValueRestDTOMapper extends RestSimpleDTOMapper<FieldValue, TwinFieldValueDTO> {

    @MapperModePointerBinding(modes = DataListOptionMode.TwinField2DataListOptionMode.class)
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
                    .hex(color.getValue());
        if (fieldValue instanceof FieldValueDate date)
            return new TwinFieldValueDateDTOv1()
                    .date(date.getDateStr());
        if (fieldValue instanceof FieldValueSelect select)
            return new TwinFieldValueListDTOv1()
                    .selectedOptions(dataListOptionRestDTOMapper.convertCollection(select.getItems(), mapperContext.forkOnPoint(mapperContext.getModeOrUse(DataListOptionMode.TwinField2DataListOptionMode.SHORT))));
        return null;
    }
}
