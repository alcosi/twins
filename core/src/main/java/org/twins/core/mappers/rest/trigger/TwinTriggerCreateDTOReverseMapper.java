package org.twins.core.mappers.rest.trigger;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.domain.trigger.TwinTriggerCreate;
import org.twins.core.dto.rest.trigger.TwinTriggerCreateDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
@RequiredArgsConstructor
public class TwinTriggerCreateDTOReverseMapper extends RestSimpleDTOMapper<TwinTriggerCreateDTOv1, TwinTriggerCreate> {
    private final TwinTriggerSaveDTOReverseMapper twinTriggerSaveDTOReverseMapper;

    @Override
    public void map(TwinTriggerCreateDTOv1 src, TwinTriggerCreate dst, MapperContext mapperContext) throws Exception {
        twinTriggerSaveDTOReverseMapper.map(src, dst, mapperContext);
    }
}
