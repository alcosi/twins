package org.twins.core.mappers.rest.twin;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.domain.twinoperation.TwinSketchCreate;
import org.twins.core.dto.rest.twin.TwinSketchCreateDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
@RequiredArgsConstructor
public class TwinSketchCreateRestDTOReverseMapper extends RestSimpleDTOMapper<TwinSketchCreateDTOv1, TwinSketchCreate> {

    private final TwinSketchSaveRestDTOReverseMapper twinSketchSaveRestDTOReverseMapper;

    @Override
    public void map(TwinSketchCreateDTOv1 src, TwinSketchCreate dst, MapperContext mapperContext) throws Exception {
        twinSketchSaveRestDTOReverseMapper.map(src, dst, mapperContext);
        dst
                .setTwinClassId(src.getClassId());
    }
}
