package org.twins.core.mappers.rest.twinflow;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twinflow.TwinflowFactoryEntity;
import org.twins.core.dto.rest.twinflow.TwinflowFactoryDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
@RequiredArgsConstructor
public class TwinflowFactoryRestDTOReverseMapper extends RestSimpleDTOMapper<TwinflowFactoryDTOv1, TwinflowFactoryEntity> {


    @Override
    public void map(TwinflowFactoryDTOv1 src, TwinflowFactoryEntity dst, MapperContext mapperContext) throws Exception {
        dst
                .setId(src.getId())
                .setTwinflowId(src.getTwinflowId())
                .setTwinFactoryId(src.getTwinFactoryId())
                .setTwinFactoryLauncher(src.getTwinFactoryLauncherId());
    }
}
