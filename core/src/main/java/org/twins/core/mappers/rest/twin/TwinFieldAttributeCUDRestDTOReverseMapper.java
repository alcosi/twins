package org.twins.core.mappers.rest.twin;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinFieldAttributeEntity;
import org.twins.core.domain.EntityCUD;
import org.twins.core.dto.rest.twin.TwinFieldAttributeCudDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.service.twin.TwinFieldAttributeService;

@Component
@RequiredArgsConstructor
public class TwinFieldAttributeCUDRestDTOReverseMapper extends RestSimpleDTOMapper<TwinFieldAttributeCudDTOv1, EntityCUD<TwinFieldAttributeEntity>> {
    private final TwinFieldAttributeCreateRestDTOReverseMapper twinFieldAttributeCreateRestDTOReverseMapper;
    private final TwinFieldAttributeUpdateRestDTOReverseMapper twinFieldAttributeUpdateRestDTOReverseMapper;
    private final TwinFieldAttributeService twinFieldAttributeService;

    @Override
    public void map(TwinFieldAttributeCudDTOv1 src, EntityCUD<TwinFieldAttributeEntity> dst, MapperContext mapperContext) throws Exception {
        dst
                .setUpdateList(twinFieldAttributeUpdateRestDTOReverseMapper.convertCollection(src.getUpdates()))
                .setCreateList(twinFieldAttributeCreateRestDTOReverseMapper.convertCollection(src.getCreates()))
                .setDeleteList(twinFieldAttributeService.findEntitiesSafe(src.getDeletes()).getList());
    }
}
