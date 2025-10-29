package org.twins.core.mappers.rest.twinflow;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twinflow.TwinflowEntity;
import org.twins.core.dto.rest.twinflow.TwinflowSaveRqDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;


@Component
@RequiredArgsConstructor
public class TwinflowSaveRestDTOReverseMapper extends RestSimpleDTOMapper<TwinflowSaveRqDTOv1, TwinflowEntity> {

    @Override
    public void map(TwinflowSaveRqDTOv1 src, TwinflowEntity dst, MapperContext mapperContext) throws Exception {
        dst
                .setInitialTwinStatusId(src.getInitialStatusId())
                .setInitialSketchTwinStatusId(src.getInitialSketchStatusId())
        ;
    }
}
