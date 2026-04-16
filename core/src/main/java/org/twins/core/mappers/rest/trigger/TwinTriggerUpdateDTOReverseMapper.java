package org.twins.core.mappers.rest.trigger;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.domain.trigger.TwinTriggerUpdate;
import org.twins.core.dto.rest.trigger.TwinTriggerUpdateDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
@RequiredArgsConstructor
public class TwinTriggerUpdateDTOReverseMapper extends RestSimpleDTOMapper<TwinTriggerUpdateDTOv1, TwinTriggerUpdate> {
    private final TwinTriggerSaveDTOReverseMapper twinTriggerSaveDTOReverseMapper;

    @Override
    public void map(TwinTriggerUpdateDTOv1 src, TwinTriggerUpdate dst, MapperContext mapperContext) throws Exception {
        twinTriggerSaveDTOReverseMapper.map(src, dst, mapperContext);
        dst.setId(src.getId());
    }
}
