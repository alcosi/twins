package org.twins.core.mappers.rest.twinflow;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twinflow.TwinflowFactoryEntity;
import org.twins.core.dto.rest.twinflow.TwinflowFactorySaveRqDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
@RequiredArgsConstructor
public class TwinflowFactorySaveRestDTOReverseMapper extends RestSimpleDTOMapper<TwinflowFactorySaveRqDTOv1, TwinflowFactoryEntity> {

    @Override
    public void map(TwinflowFactorySaveRqDTOv1 src, TwinflowFactoryEntity dst, MapperContext mapperContext) throws Exception {
        dst
                .setTwinflowId(src.getTwinflowId())
                .setTwinFactoryId(src.getTwinFactoryId())
                .setTwinFactorylauncher(src.getTwinFactoryLauncherId());
    }
}
