package org.twins.core.mappers.rest.twin;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinFieldAttributeEntity;
import org.twins.core.domain.EntityCUD;
import org.twins.core.dto.rest.twin.TwinUpdateDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.service.twin.TwinFieldAttributeService;

@Component
@RequiredArgsConstructor
public class TwinFieldAttributeCUDRestDTOReverseMapper extends RestSimpleDTOMapper<TwinUpdateDTOv1, EntityCUD<TwinFieldAttributeEntity>> {
    private final TwinFieldAttributeCreateRestDTOReverseMapper twinFieldAttributeCreateRestDTOReverseMapper;
    private final TwinFieldAttributeUpdateRestDTOReverseMapper twinFieldAttributeUpdateRestDTOReverseMapper;
    private final TwinFieldAttributeService twinFieldAttributeService;

    @Override
    public void map(TwinUpdateDTOv1 src, EntityCUD<TwinFieldAttributeEntity> dst, MapperContext mapperContext) throws Exception {
        dst
                .setUpdateList(twinFieldAttributeUpdateRestDTOReverseMapper.convertCollection(src.getFieldsAttributes().getUpdates()))
                .setCreateList(twinFieldAttributeCreateRestDTOReverseMapper.convertCollection(src.getFieldsAttributes().getCreates()))
                .setDeleteList(twinFieldAttributeService.findEntitiesSafe(src.getFieldsAttributes().getDeletes()).getList());
    }
}
