package org.twins.core.mappers.rest.twinclass;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.domain.twinclass.TwinClassFieldSave;
import org.twins.core.dto.rest.twinclass.TwinClassFieldUpdateDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
@RequiredArgsConstructor
public class TwinClassFieldUpdateRestDTOReverseMapperV2 extends RestSimpleDTOMapper<TwinClassFieldUpdateDTOv1, TwinClassFieldSave> {
    private final TwinClassFieldSaveRestDTOReverseMapperV2 twinClassFieldSaveRestDTOReverseMapper;


    @Override
    public void map(TwinClassFieldUpdateDTOv1 src, TwinClassFieldSave dst, MapperContext mapperContext) throws Exception {
        twinClassFieldSaveRestDTOReverseMapper.map(src, dst, mapperContext);
            dst.getField()
                    .setId(src.getTwinClassFieldId())
                    .setTwinClassId(src.getTwinClassId());
    }
}
