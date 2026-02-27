package org.twins.core.mappers.rest.twinstatus;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinStatusTransitionTriggerEntity;
import org.twins.core.dto.rest.twinstatus.TwinStatusTransitionTriggerCreateDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
@RequiredArgsConstructor
public class TwinStatusTransitionTriggerCreateDTOReverseMapper extends RestSimpleDTOMapper<TwinStatusTransitionTriggerCreateDTOv1, TwinStatusTransitionTriggerEntity> {
    private final TwinStatusTransitionTriggerSaveDTOReverseMapper twinStatusTransitionTriggerSaveDTOReverseMapper;

    @Override
    public void map(TwinStatusTransitionTriggerCreateDTOv1 src, TwinStatusTransitionTriggerEntity dst, MapperContext mapperContext) throws Exception {
        twinStatusTransitionTriggerSaveDTOReverseMapper.map(src, dst, mapperContext);
    }
}
