package org.twins.core.mappers.rest.twin;

import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinFieldAttributeEntity;
import org.twins.core.dto.rest.twin.TwinFieldAttributeUpdateDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
public class TwinFieldAttributeUpdateRestDTOReverseMapper extends RestSimpleDTOMapper<TwinFieldAttributeUpdateDTOv1, TwinFieldAttributeEntity> {

    @Override
    public void map(TwinFieldAttributeUpdateDTOv1 src, TwinFieldAttributeEntity dst, MapperContext mapperContext) throws Exception {
        dst
                .setId(src.getId())
                .setTwinClassFieldAttributeId(src.getTwinClassFieldAttributeId())
                .setNoteMsg(src.getMsg())
                .setNoteMsgContext(src.getContext());
    }
}
