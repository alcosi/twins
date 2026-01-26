package org.twins.core.mappers.rest.twinclass;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.domain.twinclass.TwinClassFreezeSave;
import org.twins.core.dto.rest.twinclass.TwinClassFreezeSaveDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.i18n.I18nSaveRestDTOReverseMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
@RequiredArgsConstructor
public class TwinClassFreezeSaveRestDTOReverseMapper extends RestSimpleDTOMapper<TwinClassFreezeSaveDTOv1, TwinClassFreezeSave> {
    private final I18nSaveRestDTOReverseMapper i18NSaveRestDTOReverseMapper;

    @Override
    public void map(TwinClassFreezeSaveDTOv1 src, TwinClassFreezeSave dst, MapperContext mapperContext) throws Exception {
        dst
                .setKey(src.getKey())
                .setStatusId(src.getStatusId())
                .setName(i18NSaveRestDTOReverseMapper.convert(src.getName(), mapperContext))
                .setDescription(i18NSaveRestDTOReverseMapper.convert(src.getDescription(), mapperContext));
    }
}
