package org.twins.core.mappers.rest.twinflow;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twinflow.TwinflowTransitionTriggerEntity;
import org.twins.core.domain.EntityCUD;
import org.twins.core.dto.rest.twinflow.TriggerCudDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;


@Component
@RequiredArgsConstructor
public class TriggerCUDRestDTOReverseMapperV1 extends RestSimpleDTOMapper<TriggerCudDTOv1, EntityCUD<TwinflowTransitionTriggerEntity>> {

    private final TriggerCreateRestDTOReverseMapper triggerCreateRestDTOReverseMapper;
    private final TriggerUpdateRestDTOReverseMapper triggerUpdateRestDTOReverseMapper;

    @Override
    public void map(TriggerCudDTOv1 src, EntityCUD<TwinflowTransitionTriggerEntity> dst, MapperContext mapperContext) throws Exception {
        dst
                .setUpdateList(triggerUpdateRestDTOReverseMapper.convertCollection(src.getUpdate()))
                .setCreateList(triggerCreateRestDTOReverseMapper.convertCollection(src.getCreate()))
                .setDeleteUUIDList(src.getDelete());
    }
}
