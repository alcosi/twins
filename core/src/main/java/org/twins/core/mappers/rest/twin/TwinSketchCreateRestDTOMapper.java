package org.twins.core.mappers.rest.twin;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dto.rest.twin.TwinSketchDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
@RequiredArgsConstructor
public class TwinSketchCreateRestDTOMapper extends RestSimpleDTOMapper<TwinEntity,TwinSketchDTOv1> {

    @Override
    public void map(TwinEntity src, TwinSketchDTOv1 dst, MapperContext mapperContext) throws Exception {
        dst
                .setTwinId(src.getId());
    }
}
