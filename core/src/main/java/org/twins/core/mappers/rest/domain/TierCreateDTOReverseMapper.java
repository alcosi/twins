package org.twins.core.mappers.rest.domain;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.domain.tier.TierCreate;
import org.twins.core.dto.rest.domain.TierCreateRqDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
@RequiredArgsConstructor
public class TierCreateDTOReverseMapper extends RestSimpleDTOMapper<TierCreateRqDTOv1, TierCreate> {
    private final TierSaveDTOReverseMapper tierSaveDTOReverseMapper;

    @Override
    public void map(TierCreateRqDTOv1 src, TierCreate dst, MapperContext mapperContext) throws Exception {
        tierSaveDTOReverseMapper.map(src, dst, mapperContext);
    }
}