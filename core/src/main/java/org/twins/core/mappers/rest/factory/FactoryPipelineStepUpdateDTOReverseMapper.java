package org.twins.core.mappers.rest.factory;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.dao.factory.TwinFactoryPipelineStepEntity;
import org.twins.core.dto.rest.factory.FactoryPipelineStepCreateDTOv1;
import org.twins.core.dto.rest.factory.FactoryPipelineStepUpdateDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
@RequiredArgsConstructor
public class FactoryPipelineStepUpdateDTOReverseMapper extends RestSimpleDTOMapper<FactoryPipelineStepUpdateDTOv1, TwinFactoryPipelineStepEntity> {
    private final FactoryPipelineStepSaveDTOReverseMapper factoryPipelineStepSaveDTOReverseMapper;

    @Override
    public void map(FactoryPipelineStepUpdateDTOv1 src, TwinFactoryPipelineStepEntity dst, MapperContext mapperContext) throws Exception {
        factoryPipelineStepSaveDTOReverseMapper.map(src, dst, mapperContext);
    }
}
