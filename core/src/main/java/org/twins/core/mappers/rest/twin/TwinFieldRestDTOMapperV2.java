package org.twins.core.mappers.rest.twin;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.domain.TwinField;
import org.twins.core.featurer.fieldtyper.value.FieldValue;
import org.twins.core.featurer.fieldtyper.value.FieldValueText;
import org.twins.core.mappers.rest.MapperContext;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.service.twin.TwinService;


@Component
@RequiredArgsConstructor
public class TwinFieldRestDTOMapperV2 extends RestSimpleDTOMapper<TwinField, FieldValueText> {
    final TwinService twinService;
    final TwinFieldValueRestDTOMapperV2 twinFieldValueRestDTOMapperV2;

    @Override
    public void map(TwinField src, FieldValueText dst, MapperContext mapperContext) throws Exception {
        FieldValue fieldValue = twinService.getTwinFieldValue(src);
        twinFieldValueRestDTOMapperV2.map(fieldValue, dst, mapperContext);
    }
}
