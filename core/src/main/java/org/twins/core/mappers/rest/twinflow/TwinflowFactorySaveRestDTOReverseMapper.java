package org.twins.core.mappers.rest.twinflow;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twinflow.TwinflowFactoryEntity;
import org.twins.core.dto.rest.twinflow.TwinflowFactorySaveDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
@RequiredArgsConstructor
public class TwinflowFactorySaveRestDTOReverseMapper extends RestSimpleDTOMapper<TwinflowFactorySaveDTOv1, TwinflowFactoryEntity> {

    @Override
    public void map(TwinflowFactorySaveDTOv1 src, TwinflowFactoryEntity dst, MapperContext mapperContext) throws Exception {
        dst
                .setTwinflowId(src.getTwinflowId())
                .setTwinFactoryId(src.getFactoryId())
                .setTwinFactoryLauncher(src.getTwinFactoryLauncherId());
    }
}
