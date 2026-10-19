package org.twins.core.mappers.rest.twinclass;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.domain.twinclass.TwinClassFreezeUpdate;
import org.twins.core.dto.rest.twinclass.TwinClassFreezeUpdateDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.i18n.I18nSaveRestDTOReverseMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
@RequiredArgsConstructor
public class TwinClassFreezeUpdateRestDTOReverseMapper extends RestSimpleDTOMapper<TwinClassFreezeUpdateDTOv1, TwinClassFreezeUpdate> {
    private final I18nSaveRestDTOReverseMapper i18NSaveRestDTOReverseMapper;

    @Override
    public void map(TwinClassFreezeUpdateDTOv1 src, TwinClassFreezeUpdate dst, MapperContext mapperContext) throws Exception {
        dst
                .setId(src.getId())
                .setKey(src.getKey())
                .setStatusId(src.getStatusId())
                .setName(i18NSaveRestDTOReverseMapper.convert(src.getName(), mapperContext))
                .setDescription(i18NSaveRestDTOReverseMapper.convert(src.getDescription(), mapperContext));
    }
}
