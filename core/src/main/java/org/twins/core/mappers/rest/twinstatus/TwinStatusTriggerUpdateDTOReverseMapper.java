package org.twins.core.mappers.rest.twinstatus;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinStatusTriggerEntity;
import org.twins.core.dto.rest.twinstatus.TwinStatusTriggerUpdateDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
@RequiredArgsConstructor
public class TwinStatusTriggerUpdateDTOReverseMapper extends RestSimpleDTOMapper<TwinStatusTriggerUpdateDTOv1, TwinStatusTriggerEntity> {
    private final TwinStatusTriggerSaveDTOReverseMapper twinStatusTriggerSaveDTOReverseMapper;

    @Override
    public void map(TwinStatusTriggerUpdateDTOv1 src, TwinStatusTriggerEntity dst, MapperContext mapperContext) throws Exception {
        twinStatusTriggerSaveDTOReverseMapper.map(src, dst, mapperContext);
        dst.setId(src.getId());
    }
}
