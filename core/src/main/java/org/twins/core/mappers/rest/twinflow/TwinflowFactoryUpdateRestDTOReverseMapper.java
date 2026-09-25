package org.twins.core.mappers.rest.twinflow;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twinflow.TwinflowFactoryEntity;
import org.twins.core.dto.rest.twinflow.TwinflowFactoryUpdateDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
@RequiredArgsConstructor
public class TwinflowFactoryUpdateRestDTOReverseMapper extends RestSimpleDTOMapper<TwinflowFactoryUpdateDTOv1, TwinflowFactoryEntity> {

    public void map(TwinflowFactoryUpdateDTOv1 src, TwinflowFactoryEntity dst, MapperContext mapperContext) throws Exception {
        dst
                .setId(src.getId())
                .setTwinflowId(src.getTwinflowId())
                .setTwinFactoryId(src.getFactoryId())
                .setTwinFactoryLauncher(src.getTwinFactoryLauncherId());
    }
}
