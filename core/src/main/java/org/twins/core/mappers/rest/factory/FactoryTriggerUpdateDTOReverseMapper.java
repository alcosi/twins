package org.twins.core.mappers.rest.factory;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.dao.factory.TwinFactoryTriggerEntity;
import org.twins.core.dto.rest.twinflow.TwinFactoryTriggerUpdateDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
@RequiredArgsConstructor
public class FactoryTriggerUpdateDTOReverseMapper extends RestSimpleDTOMapper<TwinFactoryTriggerUpdateDTOv1, TwinFactoryTriggerEntity> {
    private final FactoryTriggerSaveDTOReverseMapper factoryTriggerSaveDTOReverseMapper;

    @Override
    public void map(TwinFactoryTriggerUpdateDTOv1 src, TwinFactoryTriggerEntity dst, MapperContext mapperContext) throws Exception {
        factoryTriggerSaveDTOReverseMapper.map(src, dst, mapperContext);
        dst.setId(src.getId());
    }
}
