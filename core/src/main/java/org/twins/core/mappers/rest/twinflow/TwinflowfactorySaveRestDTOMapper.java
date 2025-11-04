package org.twins.core.mappers.rest.twinflow;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twinflow.TwinflowFactoryEntity;
import org.twins.core.dto.rest.twinflow.TwinflowFactorySaveDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
@RequiredArgsConstructor
public class TwinflowfactorySaveRestDTOMapper extends RestSimpleDTOMapper<TwinflowFactoryEntity, TwinflowFactorySaveDTOv1> {

    @Override
    public void map(TwinflowFactoryEntity src, TwinflowFactorySaveDTOv1 dst, MapperContext mapperContext) throws Exception {
        dst
                .setTwinflowId(src.getTwinflowId())
                .setTwinFactoryId(src.getTwinFactoryId())
                .setTwinFactoryLauncherId(src.getTwinFactoryLauncher());
    }
}
