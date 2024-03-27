package org.twins.core.mappers.rest.twin;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.dto.rest.twin.TwinFieldDTOv1;
import org.twins.core.featurer.fieldtyper.value.FieldValue;
import org.twins.core.mappers.rest.MapperContext;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.twinclass.TwinClassFieldRestDTOMapper;
import org.twins.core.mappers.rest.user.UserRestDTOMapper;
import org.twins.core.service.twin.TwinService;


@Component
@RequiredArgsConstructor
public class TwinFieldRestDTOMapperV3 extends RestSimpleDTOMapper<FieldValue, TwinFieldDTOv1> {
    final UserRestDTOMapper userDTOMapper;
    final TwinClassFieldRestDTOMapper twinClassFieldRestDTOMapper;
    final TwinService twinService;
    final TwinFieldValueRestDTOMapper twinFieldValueRestDTOMapper;

    @Override
    public void map(FieldValue src, TwinFieldDTOv1 dst, MapperContext mapperContext) throws Exception {
        dst
                .twinClassField(twinClassFieldRestDTOMapper.convert(src.getTwinClassField(), mapperContext.cloneWithIsolatedModes().setModeIfNotPresent(TwinClassFieldRestDTOMapper.Mode.SHORT)))
                .value(twinFieldValueRestDTOMapper.convert(src));
    }
}
