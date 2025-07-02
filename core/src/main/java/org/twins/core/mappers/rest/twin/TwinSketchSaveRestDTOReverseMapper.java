package org.twins.core.mappers.rest.twin;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.domain.twinoperation.TwinSketchSave;
import org.twins.core.dto.rest.twin.TwinSketchSaveDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
@RequiredArgsConstructor
public class TwinSketchSaveRestDTOReverseMapper extends RestSimpleDTOMapper<TwinSketchSaveDTOv1, TwinSketchSave> {

    private final TwinFieldValueRestDTOReverseMapperV2 twinFieldValueRestDTOReverseMapperV2;

    @Override
    public void map(TwinSketchSaveDTOv1 src, TwinSketchSave dst, MapperContext mapperContext) throws Exception {
        dst
                .setFields(twinFieldValueRestDTOReverseMapperV2.mapFields(src.getClassId(), src.getFields()));
    }
}
