package org.twins.core.mappers.rest.twinclass;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.domain.twinclass.TwinClassFreezeCreate;
import org.twins.core.dto.rest.twinclass.TwinClassFreezeCreateDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.i18n.I18nSaveRestDTOReverseMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
@RequiredArgsConstructor
public class TwinClassFreezeCreateRestDTOReverseMapper extends RestSimpleDTOMapper<TwinClassFreezeCreateDTOv1, TwinClassFreezeCreate> {
    private final I18nSaveRestDTOReverseMapper i18NSaveRestDTOReverseMapper;

    @Override
    public void map(TwinClassFreezeCreateDTOv1 src, TwinClassFreezeCreate dst, MapperContext mapperContext) throws Exception {
        dst
                .setKey(src.getKey())
                .setStatusId(src.getStatusId())
                .setName(i18NSaveRestDTOReverseMapper.convert(src.getName(), mapperContext))
                .setDescription(i18NSaveRestDTOReverseMapper.convert(src.getDescription(), mapperContext));
    }
}
