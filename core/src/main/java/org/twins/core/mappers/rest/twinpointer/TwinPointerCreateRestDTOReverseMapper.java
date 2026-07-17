package org.twins.core.mappers.rest.twinpointer;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.domain.twin.TwinPointerCreate;
import org.twins.core.dto.rest.twinpointer.TwinPointerCreateDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
@RequiredArgsConstructor
public class TwinPointerCreateRestDTOReverseMapper extends RestSimpleDTOMapper<TwinPointerCreateDTOv1, TwinPointerCreate> {
    private final TwinPointerSaveRestDTOReverseMapper twinPointerSaveRestDTOReverseMapper;

    @Override
    public void map(TwinPointerCreateDTOv1 src, TwinPointerCreate dst, MapperContext mapperContext) throws Exception {
        twinPointerSaveRestDTOReverseMapper.map(src, dst, mapperContext);
    }
}
