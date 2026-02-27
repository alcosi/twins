package org.twins.core.mappers.rest.twinstatus;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinStatusTransitionTriggerEntity;
import org.twins.core.dto.rest.twinstatus.TwinStatusTransitionTriggerUpdateDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
@RequiredArgsConstructor
public class TwinStatusTransitionTriggerUpdateDTOReverseMapper extends RestSimpleDTOMapper<TwinStatusTransitionTriggerUpdateDTOv1, TwinStatusTransitionTriggerEntity> {
    private final TwinStatusTransitionTriggerSaveDTOReverseMapper twinStatusTransitionTriggerSaveDTOReverseMapper;

    @Override
    public void map(TwinStatusTransitionTriggerUpdateDTOv1 src, TwinStatusTransitionTriggerEntity dst, MapperContext mapperContext) throws Exception {
        twinStatusTransitionTriggerSaveDTOReverseMapper.map(src, dst, mapperContext);
        dst.setId(src.getId());
    }
}
