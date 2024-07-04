package org.twins.core.mappers.rest.twin;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModePointerBinding;
import org.twins.core.domain.TwinField;
import org.twins.core.dto.rest.twin.TwinFieldDTOv1;
import org.twins.core.featurer.fieldtyper.value.FieldValue;
import org.twins.core.mappers.rest.MapperContext;
import org.twins.core.mappers.rest.MapperMode;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.twinclass.TwinClassFieldRestDTOMapper;
import org.twins.core.service.twin.TwinService;


@Component
@RequiredArgsConstructor
public class TwinFieldRestDTOMapper extends RestSimpleDTOMapper<TwinField, TwinFieldDTOv1> {

    @MapperModePointerBinding(modes = MapperMode.TwinClassFieldOnTwinFieldMode.class)
    private final TwinClassFieldRestDTOMapper twinClassFieldRestDTOMapper;

    private final TwinFieldValueRestDTOMapper twinFieldValueRestDTOMapper;

    private final TwinService twinService;

    @Override
    public void map(TwinField src, TwinFieldDTOv1 dst, MapperContext mapperContext) throws Exception {
        FieldValue fieldValue = twinService.getTwinFieldValue(src);
        dst
                .twinClassField(twinClassFieldRestDTOMapper.convert(src.getTwinClassField(), mapperContext.forkOnPoint(mapperContext.getModeOrUse(MapperMode.TwinClassFieldOnTwinFieldMode.HIDE))))
                .value(twinFieldValueRestDTOMapper.convert(fieldValue));
    }
}
