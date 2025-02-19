package org.twins.core.mappers.rest.factory;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.dao.factory.TwinFactoryPipelineEntity;
import org.twins.core.dto.rest.factory.FactoryPipelineUpdateRqDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
@RequiredArgsConstructor
public class FactoryPipelineUpdateDTOReverseMapper extends RestSimpleDTOMapper<FactoryPipelineUpdateRqDTOv1, TwinFactoryPipelineEntity> {
    private final FactoryPipelineSaveDTOReverseMapper mapper;

    @Override
    public void map(FactoryPipelineUpdateRqDTOv1 src, TwinFactoryPipelineEntity dst, MapperContext mapperContext) throws Exception {
        mapper.map(src.getFactoryPipelineSaveDTO(), dst, mapperContext);
    }
}
