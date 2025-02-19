package org.twins.core.mappers.rest.factory;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.dao.factory.TwinFactoryPipelineEntity;
import org.twins.core.dto.rest.factory.FactoryPipelineCreateRqDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
@RequiredArgsConstructor
public class FactoryPipelineCreateDTOReverseMapper extends RestSimpleDTOMapper<FactoryPipelineCreateRqDTOv1, TwinFactoryPipelineEntity> {
    private final FactoryPipelineSaveDTOReverseMapper mapper;

    @Override
    public void map(FactoryPipelineCreateRqDTOv1 src, TwinFactoryPipelineEntity dst, MapperContext mapperContext) throws Exception {
        mapper.map(src.getFactoryPipelineSaveDTO(), dst, mapperContext);
    }
}
