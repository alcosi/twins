package org.twins.core.mappers.rest.twinflow;

import org.springframework.stereotype.Component;
import org.twins.core.dao.twinflow.TwinflowEntity;
import org.twins.core.dto.rest.twinflow.TwinflowCreateDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;


@Component
public class TwinflowCreateRestDTOReverseMapper extends RestSimpleDTOMapper<TwinflowCreateDTOv1, TwinflowEntity> {

    @Override
    public void map(TwinflowCreateDTOv1 src, TwinflowEntity dst, MapperContext mapperContext) throws Exception {
        dst
                .setTwinClassId(src.getTwinClassId())
                .setInitialTwinStatusId(src.getInitialStatusId())
                .setInitialSketchTwinStatusId(src.getInitialSketchStatusId())
                .setInheritable(src.getInheritable());
    }
}
