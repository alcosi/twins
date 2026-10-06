package org.twins.core.mappers.rest.twin;

import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinFieldAttributeEntity;
import org.twins.core.dto.rest.twin.TwinFieldAttributeCreateDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
public class TwinFieldAttributeCreateRestDTOReverseMapper extends RestSimpleDTOMapper<TwinFieldAttributeCreateDTOv1, TwinFieldAttributeEntity> {

    @Override
    public void map(TwinFieldAttributeCreateDTOv1 src, TwinFieldAttributeEntity dst, MapperContext mapperContext) throws Exception {
        dst
                .setTwinClassFieldId(src.getTwinClassFieldId())
                .setTwinClassFieldAttributeId(src.getTwinClassFieldAttributeId())
                .setNoteMsg(src.getMsg())
                .setNoteMsgContext(src.getContext());
    }
}
