package org.twins.core.mappers.rest.twin;

import lombok.RequiredArgsConstructor;
import org.cambium.common.exception.ErrorCodeCommon;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.FeaturerService;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinFieldEntity;
import org.twins.core.dto.rest.twin.*;
import org.twins.core.featurer.fieldtyper.*;
import org.twins.core.mappers.rest.MapperMode;
import org.twins.core.mappers.rest.MapperProperties;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.datalist.DataListOptionRestDTOMapper;
import org.twins.core.mappers.rest.twinclass.TwinClassFieldRestDTOMapper;
import org.twins.core.mappers.rest.twinclass.TwinClassRestDTOMapper;
import org.twins.core.mappers.rest.user.UserDTOMapper;


@Component
@RequiredArgsConstructor
public class TwinFieldValueRestDTOMapper extends RestSimpleDTOMapper<FieldValue, TwinFieldValueDTO> {
    final DataListOptionRestDTOMapper dataListOptionRestDTOMapper;

    @Override
    public void map(FieldValue src, TwinFieldValueDTO dst, MapperProperties mapperProperties) throws Exception {
        throw new ServiceException(ErrorCodeCommon.NOT_IMPLEMENTED);
    }

    @Override
    public TwinFieldValueDTO convert(FieldValue fieldValue, MapperProperties mapperProperties) throws Exception {
        if (fieldValue instanceof FieldValueText text)
            return new TwinFieldValueTextDTOv1()
                    .text(text.value());
        if (fieldValue instanceof FieldValueColorHEX color)
            return new TwinFieldValueColorHexDTOv1()
                    .hex(color.hex());
        if (fieldValue instanceof FieldValueDate date)
            return new TwinFieldValueDateDTOv1()
                    .date(date.date());
        if (fieldValue instanceof FieldValueSelect select)
            return new TwinFieldValueDataListOptionsDTOv1()
                    .selectedOptions(dataListOptionRestDTOMapper.convertList(select.options(), new MapperProperties().setMode(DataListOptionRestDTOMapper.Mode.ID_NAME_ONLY)));
        return null;
    }
}
