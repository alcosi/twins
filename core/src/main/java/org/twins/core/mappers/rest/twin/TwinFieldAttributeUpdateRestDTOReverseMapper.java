package org.twins.core.mappers.rest.twin;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinFieldAttributeEntity;
import org.twins.core.dto.rest.twin.TwinFieldAttributeUpdateDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
@RequiredArgsConstructor
public class TwinFieldAttributeUpdateRestDTOReverseMapper extends RestSimpleDTOMapper<TwinFieldAttributeUpdateDTOv1, TwinFieldAttributeEntity> {
    private final TwinFieldAttributeSaveRestDTOReverseMapper twinFieldAttributeSaveRestDTOReverseMapper;


    @Override
    public void map(TwinFieldAttributeUpdateDTOv1 src, TwinFieldAttributeEntity dst, MapperContext mapperContext) throws Exception {
        twinFieldAttributeSaveRestDTOReverseMapper.map(src, dst, mapperContext);
        dst.setId(src.getId());
    }
}
