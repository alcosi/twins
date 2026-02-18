package org.twins.core.mappers.rest.twinflow;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.dao.factory.TwinFactoryTriggerEntity;
import org.twins.core.dto.rest.twinflow.TwinFactoryTriggerCreateDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
@RequiredArgsConstructor
public class TwinFactoryTriggerCreateDTOReverseMapper extends RestSimpleDTOMapper<TwinFactoryTriggerCreateDTOv1, TwinFactoryTriggerEntity> {
    private final TwinFactoryTriggerSaveDTOReverseMapper twinFactoryTriggerSaveDTOReverseMapper;

    @Override
    public void map(TwinFactoryTriggerCreateDTOv1 src, TwinFactoryTriggerEntity dst, MapperContext mapperContext) throws Exception {
        twinFactoryTriggerSaveDTOReverseMapper.map(src, dst, mapperContext);
    }
}
