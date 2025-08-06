package org.twins.core.mappers.rest.twin;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.domain.twinoperation.TwinUpdateClass;
import org.twins.core.dto.rest.twin.TwinUpdateClassDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
@RequiredArgsConstructor
public class TwinUpdateClassRestDTOReverseMapper extends RestSimpleDTOMapper<TwinUpdateClassDTOv1, TwinUpdateClass> {

    @Override
    public void map(TwinUpdateClassDTOv1 src, TwinUpdateClass dst, MapperContext mapperContext) throws Exception {
        dst
                .setNewTwinClassId(src.getNewTwinClassId())
                .setNewHeadTwinId(src.getNewHeadTwinId())
                .setFieldsReplaceMap(src.getFieldsReplaceMap())
                .setBehavior(src.getBehavior());

    }
}
