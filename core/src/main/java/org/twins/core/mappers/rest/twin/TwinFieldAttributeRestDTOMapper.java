package org.twins.core.mappers.rest.twin;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinFieldAttributeEntity;
import org.twins.core.dto.rest.twin.TwinFieldAttributeDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.service.i18n.I18nService;

import java.util.Locale;

@Component
@RequiredArgsConstructor
public class TwinFieldAttributeRestDTOMapper extends RestSimpleDTOMapper<TwinFieldAttributeEntity, TwinFieldAttributeDTOv1> {
    private final I18nService i18nService;

    @Override
    public void map(TwinFieldAttributeEntity src, TwinFieldAttributeDTOv1 dst, MapperContext mapperContext) throws Exception {
        dst
                .setId(src.getId())
                .setNoteMsg(src.getNoteMsg() != null ? src.getNoteMsg() : i18nService.translateToLocale(src.getTwinClassFieldAttributeEntity().getNoteMsgI18nOd(), src.getNoteMsgContext()))
                .setTwinClassFieldAttributeId(src.getTwinClassFieldAttributeId())
                .setChangedAt(src.getChangedAt().toLocalDateTime());
    }
}
