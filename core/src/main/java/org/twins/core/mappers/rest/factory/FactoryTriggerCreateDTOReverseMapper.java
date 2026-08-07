package org.twins.core.mappers.rest.factory;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.dao.factory.TwinFactoryTriggerEntity;
import org.twins.core.dto.rest.twinflow.TwinFactoryTriggerCreateDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
@RequiredArgsConstructor
public class FactoryTriggerCreateDTOReverseMapper extends RestSimpleDTOMapper<TwinFactoryTriggerCreateDTOv1, TwinFactoryTriggerEntity> {
    private final FactoryTriggerSaveDTOReverseMapper factoryTriggerSaveDTOReverseMapper;

    @Override
    public void map(TwinFactoryTriggerCreateDTOv1 src, TwinFactoryTriggerEntity dst, MapperContext mapperContext) throws Exception {
        factoryTriggerSaveDTOReverseMapper.map(src, dst, mapperContext);
    }
}
