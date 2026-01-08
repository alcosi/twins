package org.twins.core.mappers.rest.twinclass;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.domain.twinclass.TwinClassFreezeCreate;
import org.twins.core.dto.rest.twinclass.TwinClassFreezeCreateDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
@RequiredArgsConstructor
public class TwinClassFreezeCreateRestDTOReverseMapper extends RestSimpleDTOMapper<TwinClassFreezeCreateDTOv1, TwinClassFreezeCreate> {
    private final TwinClassFreezeSaveRestDTOReverseMapper twinClassFreezeSaveRestDTOReverseMapper;

    @Override
    public void map(TwinClassFreezeCreateDTOv1 src, TwinClassFreezeCreate dst, MapperContext mapperContext) throws Exception {
        twinClassFreezeSaveRestDTOReverseMapper.map(src, dst, mapperContext);
    }
}
