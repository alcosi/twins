package org.twins.core.mappers.rest.twinflow;

import org.springframework.stereotype.Component;
import org.twins.core.dao.twinflow.TwinflowEntity;
import org.twins.core.dto.rest.twinflow.TwinflowCreateRqDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;


@Component
public class TwinflowCreateRestDTOReverseMapper extends RestSimpleDTOMapper<TwinflowCreateRqDTOv1, TwinflowEntity> {

    @Override
    public void map(TwinflowCreateRqDTOv1 src, TwinflowEntity dst, MapperContext mapperContext) throws Exception {
        dst
                .setInitialTwinStatusId(src.getInitialStatusId())
                .setInitialSketchTwinStatusId(src.getInitialSketchStatusId())
                .setInheritable(src.getInheritable());
    }
}
