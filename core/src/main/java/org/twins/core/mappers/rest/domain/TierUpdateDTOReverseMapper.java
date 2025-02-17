package org.twins.core.mappers.rest.domain;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.domain.tier.TierUpdate;
import org.twins.core.dto.rest.domain.TierUpdateRqDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
@RequiredArgsConstructor
public class TierUpdateDTOReverseMapper extends RestSimpleDTOMapper<TierUpdateRqDTOv1, TierUpdate> {
    private final TierSaveDTOReverseMapper tierSaveDTOReverseMapper;

    @Override
    public void map(TierUpdateRqDTOv1 src, TierUpdate dst, MapperContext mapperContext) throws Exception {
        tierSaveDTOReverseMapper.map(src, dst, mapperContext);
        dst.setId(src.getId());
    }
}