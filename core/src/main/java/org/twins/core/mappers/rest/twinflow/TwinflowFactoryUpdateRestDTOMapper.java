package org.twins.core.mappers.rest.twinflow;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twinflow.TwinflowFactoryEntity;
import org.twins.core.dto.rest.twinflow.TwinflowFactoryUpdateDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
@RequiredArgsConstructor
public class TwinflowFactoryUpdateRestDTOMapper extends RestSimpleDTOMapper<TwinflowFactoryEntity, TwinflowFactoryUpdateDTOv1> {

    private final TwinflowfactorySaveRestDTOMapper twinflowfactorySaveRestDTOMapper;

    @Override
    public void map(TwinflowFactoryEntity src, TwinflowFactoryUpdateDTOv1 dst, MapperContext mapperContext) throws Exception {
        dst.setId(src.getId());
        twinflowfactorySaveRestDTOMapper.map(src, dst, mapperContext);
    }
}
