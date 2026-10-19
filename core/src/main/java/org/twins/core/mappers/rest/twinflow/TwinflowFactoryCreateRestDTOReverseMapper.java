package org.twins.core.mappers.rest.twinflow;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twinflow.TwinflowFactoryEntity;
import org.twins.core.dto.rest.twinflow.TwinflowFactoryCreateDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
@RequiredArgsConstructor
public class TwinflowFactoryCreateRestDTOReverseMapper extends RestSimpleDTOMapper<TwinflowFactoryCreateDTOv1, TwinflowFactoryEntity> {

    @Override
    public void map(TwinflowFactoryCreateDTOv1 src, TwinflowFactoryEntity dst, MapperContext mapperContext) throws Exception {
        dst
                .setTwinflowId(src.getTwinflowId())
                .setTwinFactoryId(src.getFactoryId())
                .setTwinFactoryLauncher(src.getTwinFactoryLauncherId());
    }
}
