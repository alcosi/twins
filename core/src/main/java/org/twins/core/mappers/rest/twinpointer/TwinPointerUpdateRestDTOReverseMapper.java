package org.twins.core.mappers.rest.twinpointer;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.domain.twin.TwinPointerUpdate;
import org.twins.core.dto.rest.twinpointer.TwinPointerUpdateDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
@RequiredArgsConstructor
public class TwinPointerUpdateRestDTOReverseMapper extends RestSimpleDTOMapper<TwinPointerUpdateDTOv1, TwinPointerUpdate> {
    private final TwinPointerSaveRestDTOReverseMapper twinPointerSaveRestDTOReverseMapper;

    @Override
    public void map(TwinPointerUpdateDTOv1 src, TwinPointerUpdate dst, MapperContext mapperContext) throws Exception {
        twinPointerSaveRestDTOReverseMapper.map(src, dst, mapperContext);
        dst.getTwinPointer().setId(src.getId());
    }
}
