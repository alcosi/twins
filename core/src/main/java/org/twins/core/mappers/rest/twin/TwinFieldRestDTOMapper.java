package org.twins.core.mappers.rest.twin;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModePointerBinding;
import org.twins.core.domain.TwinField;
import org.twins.core.dto.rest.twin.TwinFieldDTOv1;
import org.twins.core.featurer.fieldtyper.value.FieldValue;
import org.twins.core.mappers.rest.mappercontext.modes.ClassFieldMode;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.twinclass.TwinClassFieldRestDTOMapper;
import org.twins.core.service.twin.TwinService;


@Component
@RequiredArgsConstructor
public class TwinFieldRestDTOMapper extends RestSimpleDTOMapper<TwinField, TwinFieldDTOv1> {

    @MapperModePointerBinding(modes = ClassFieldMode.TwinFieldOnClassFieldMode.class)
    private final TwinClassFieldRestDTOMapper twinClassFieldRestDTOMapper;

    private final TwinFieldValueRestDTOMapper twinFieldValueRestDTOMapper;

    private final TwinService twinService;

    @Override
    public void map(TwinField src, TwinFieldDTOv1 dst, MapperContext mapperContext) throws Exception {
        FieldValue fieldValue = twinService.getTwinFieldValue(src);
        dst.value(twinFieldValueRestDTOMapper.convert(fieldValue));
        if (mapperContext.hasModeButNot(ClassFieldMode.TwinFieldOnClassFieldMode.HIDE))
            dst
                    .twinClassField(twinClassFieldRestDTOMapper.convert(src.getTwinClassField(), mapperContext.cloneWithIsolatedModes().setModeIfNotPresent(ClassFieldMode.TwinFieldOnClassFieldMode.SHORT)));
    }
}
