package org.twins.core.mappers.rest.twinclass;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.dto.rest.twinclass.TwinClassFieldUpdateRqDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
@RequiredArgsConstructor
public class TwinClassFieldUpdateRestDTOReverseMapper extends RestSimpleDTOMapper<TwinClassFieldUpdateRqDTOv1, TwinClassFieldEntity> {

    private final TwinClassFieldSaveRestDTOReverseMapper twinClassFieldSaveRestDTOReverseMapper;

    @Override
    public void map(TwinClassFieldUpdateRqDTOv1 src, TwinClassFieldEntity dst, MapperContext mapperContext) throws Exception {
        twinClassFieldSaveRestDTOReverseMapper.map(src, dst, mapperContext);
        dst
                .setTwinClassId(src.getTwinClassId());
    }
}
