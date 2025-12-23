package org.twins.core.mappers.rest.twin;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinFieldAttributeEntity;
import org.twins.core.dto.rest.twin.TwinFieldAttributeDTOv1;
import org.twins.core.holder.I18nCacheHolder;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
@RequiredArgsConstructor
public class TwinFieldAttributeRestDTOMapper extends RestSimpleDTOMapper<TwinFieldAttributeEntity, TwinFieldAttributeDTOv1> {

    @Override
    public void map(TwinFieldAttributeEntity src, TwinFieldAttributeDTOv1 dst, MapperContext mapperContext) throws Exception {
        dst
                .setId(src.getId())
                .setNoteMsg(src.getNoteMsg() != null ? src.getNoteMsg() : I18nCacheHolder.addId(src.getTwinClassFieldAttribute().getNoteMsgI18nId(), src.getNoteMsgContext()))
                .setContext(src.getNoteMsgContext())
                .setTwinClassFieldAttributeId(src.getTwinClassFieldAttributeId())
                .setChangedAt(src.getChangedAt().toLocalDateTime());
    }
}
