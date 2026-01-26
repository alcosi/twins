package org.twins.core.mappers.rest.twin;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.domain.twinoperation.TwinChangeClass;
import org.twins.core.dto.rest.twin.TwinChangeClassDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
@RequiredArgsConstructor
public class TwinChangeClassRestDTOReverseMapper extends RestSimpleDTOMapper<TwinChangeClassDTOv1, TwinChangeClass> {

    @Override
    public void map(TwinChangeClassDTOv1 src, TwinChangeClass dst, MapperContext mapperContext) throws Exception {
        dst
                .setNewTwinClassId(src.getNewTwinClassId())
                .setNewHeadTwinId(src.getNewHeadTwinId())
                .setFieldsReplaceMap(src.getFieldsReplaceMap())
                .setBehavior(src.getBehavior());

    }
}
