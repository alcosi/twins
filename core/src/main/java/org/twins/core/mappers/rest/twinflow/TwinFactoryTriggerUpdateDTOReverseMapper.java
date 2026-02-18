package org.twins.core.mappers.rest.twinflow;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.dao.factory.TwinFactoryTriggerEntity;
import org.twins.core.dto.rest.twinflow.TwinFactoryTriggerUpdateDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
@RequiredArgsConstructor
public class TwinFactoryTriggerUpdateDTOReverseMapper extends RestSimpleDTOMapper<TwinFactoryTriggerUpdateDTOv1, TwinFactoryTriggerEntity> {
    private final TwinFactoryTriggerSaveDTOReverseMapper twinFactoryTriggerSaveDTOReverseMapper;

    @Override
    public void map(TwinFactoryTriggerUpdateDTOv1 src, TwinFactoryTriggerEntity dst, MapperContext mapperContext) throws Exception {
        twinFactoryTriggerSaveDTOReverseMapper.map(src, dst, mapperContext);
        dst.setId(src.getId());
    }
}
