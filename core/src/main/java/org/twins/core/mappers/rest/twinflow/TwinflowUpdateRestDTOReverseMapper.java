package org.twins.core.mappers.rest.twinflow;

import org.springframework.stereotype.Component;
import org.twins.core.dao.twinflow.TwinflowEntity;
import org.twins.core.dto.rest.twinflow.TwinflowUpdateRqDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;


@Component
public class TwinflowUpdateRestDTOReverseMapper extends RestSimpleDTOMapper<TwinflowUpdateRqDTOv1, TwinflowEntity> {

    @Override
    public void map(TwinflowUpdateRqDTOv1 src, TwinflowEntity dst, MapperContext mapperContext) throws Exception {
        dst
                .setInitialTwinStatusId(src.getInitialStatusId())
                .setInitialSketchTwinStatusId(src.getInitialSketchStatusId())
                .setInheritable(src.getInheritable());
    }
}
