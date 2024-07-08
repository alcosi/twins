package org.twins.core.mappers.rest.twin;

import lombok.RequiredArgsConstructor;
import org.cambium.common.exception.ErrorCodeCommon;
import org.cambium.common.exception.ServiceException;
import org.springframework.stereotype.Component;
import org.twins.core.domain.TwinField;
import org.twins.core.featurer.fieldtyper.value.FieldValue;
import org.twins.core.featurer.fieldtyper.value.FieldValueText;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.service.twin.TwinService;


@Component
@RequiredArgsConstructor
public class TwinFieldRestDTOMapperV2 extends RestSimpleDTOMapper<TwinField, FieldValueText> {

    private final TwinFieldValueRestDTOMapperV2 twinFieldValueRestDTOMapperV2;

    private final TwinService twinService;

    @Override
    public FieldValueText convert(TwinField src, MapperContext mapperContext) throws Exception {
        FieldValue fieldValue = twinService.getTwinFieldValue(src);
        return twinFieldValueRestDTOMapperV2.convert(fieldValue, mapperContext);
    }

    @Override
    public void map(TwinField src, FieldValueText dst, MapperContext mapperContext) throws Exception {
        throw new ServiceException(ErrorCodeCommon.NOT_IMPLEMENTED);
    }
}
