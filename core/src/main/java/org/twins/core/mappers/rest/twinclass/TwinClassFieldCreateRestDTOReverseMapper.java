package org.twins.core.mappers.rest.twinclass;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.dto.rest.twinclass.TwinClassFieldCreateDTOv1;
import org.twins.core.dto.rest.twinclass.TwinClassFieldCreateRqDTOv1;
import org.twins.core.dto.rest.twinclass.TwinClassFieldSave;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
@RequiredArgsConstructor
public class TwinClassFieldCreateRestDTOReverseMapper extends RestSimpleDTOMapper<TwinClassFieldCreateDTOv1, TwinClassFieldSave> {
    private final TwinClassFieldSaveRestDTOReverseMapper twinClassFieldSaveRestDTOReverseMapper;

    @Override
    public void map(TwinClassFieldCreateDTOv1 src, TwinClassFieldSave dst, MapperContext mapperContext) throws Exception {
        twinClassFieldSaveRestDTOReverseMapper.map(src, dst, mapperContext);
        if (dst.getField() != null)
            dst.getField().setTwinClassId(src.getTwinClassId());
    }
}
