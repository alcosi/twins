package org.twins.core.mappers.rest.twin;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinFieldAttributeEntity;
import org.twins.core.dto.rest.twin.TwinFieldAttributeSaveDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
@RequiredArgsConstructor
public class TwinFieldAttributeSaveRestDTOReverseMapper extends RestSimpleDTOMapper<TwinFieldAttributeSaveDTOv1, TwinFieldAttributeEntity> {

    @Override
    public void map(TwinFieldAttributeSaveDTOv1 src, TwinFieldAttributeEntity dst, MapperContext mapperContext) throws Exception {
        dst
                .setTwinClassFieldAttributeId(src.getTwinClassFieldAttributeId())
                .setNoteMsg(src.getMsg())
                .setNoteMsgContext(src.getContext());
    }
}
