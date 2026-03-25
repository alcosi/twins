package org.twins.core.mappers.rest.trigger;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.domain.trigger.TwinTriggerSave;
import org.twins.core.dto.rest.trigger.TwinTriggerSaveDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
@RequiredArgsConstructor
public class TwinTriggerSaveDTOReverseMapper extends RestSimpleDTOMapper<TwinTriggerSaveDTOv1, TwinTriggerSave> {

    @Override
    public void map(TwinTriggerSaveDTOv1 src, TwinTriggerSave dst, MapperContext mapperContext) throws Exception {
        dst
                .setTriggerFeaturerId(src.getTriggerFeaturerId())
                .setTriggerParams(src.getTriggerParams())
                .setName(src.getName())
                .setDescription(src.getDescription())
                .setActive(src.getActive());
    }
}
