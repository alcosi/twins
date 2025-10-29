package org.twins.core.mappers.rest.twinclass;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.domain.twinclass.TwinClassFieldSave;
import org.twins.core.dto.rest.twinclass.TwinClassFieldUpdateRqDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Deprecated
@Component
@RequiredArgsConstructor
public class TwinClassFieldUpdateRestDTOReverseMapper extends RestSimpleDTOMapper<TwinClassFieldUpdateRqDTOv1, TwinClassFieldSave> {
    private final TwinClassFieldSaveRestDTOReverseMapper twinClassFieldSaveRestDTOReverseMapper;


    @Override
    public void map(TwinClassFieldUpdateRqDTOv1 src, TwinClassFieldSave dst, MapperContext mapperContext) throws Exception {
        twinClassFieldSaveRestDTOReverseMapper.map(src, dst, mapperContext);
        dst.getField()
                .setId(src.getTwinClassFieldId())
                .setTwinClassId(src.getTwinClassId());
    }
}
