package org.twins.core.mappers.rest.twin;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModePointerBinding;
import org.twins.core.dto.rest.twin.TwinFieldDTOv1;
import org.twins.core.featurer.fieldtyper.value.FieldValue;
import org.twins.core.mappers.rest.mappercontext.modes.ClassFieldMode;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.twinclass.TwinClassFieldRestDTOMapper;


@Component
@RequiredArgsConstructor
public class TwinFieldRestDTOMapperV3 extends RestSimpleDTOMapper<FieldValue, TwinFieldDTOv1> {

    @MapperModePointerBinding(modes = ClassFieldMode.TwinFieldOnClassFieldMode.class)
    private final TwinClassFieldRestDTOMapper twinClassFieldRestDTOMapper;

    private final TwinFieldValueRestDTOMapper twinFieldValueRestDTOMapper;

    @Override
    public void map(FieldValue src, TwinFieldDTOv1 dst, MapperContext mapperContext) throws Exception {
        dst.value(twinFieldValueRestDTOMapper.convert(src));
        if (mapperContext.hasModeButNot(ClassFieldMode.TwinFieldOnClassFieldMode.HIDE))
            dst
                    .twinClassField(twinClassFieldRestDTOMapper.convert(src.getTwinClassField(), mapperContext.cloneWithIsolatedModes().setModeIfNotPresent(ClassFieldMode.TwinFieldOnClassFieldMode.SHORT)));
    }
}
