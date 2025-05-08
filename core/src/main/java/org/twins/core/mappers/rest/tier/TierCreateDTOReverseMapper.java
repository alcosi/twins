package org.twins.core.mappers.rest.tier;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.dao.domain.TierEntity;
import org.twins.core.dto.rest.tier.TierCreateDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
@RequiredArgsConstructor
public class TierCreateDTOReverseMapper extends RestSimpleDTOMapper<TierCreateDTOv1, TierEntity> {
    private final TierSaveDTOReverseMapper tierSaveDTOReverseMapper;

    @Override
    public void map(TierCreateDTOv1 src, TierEntity dst, MapperContext mapperContext) throws Exception {
        tierSaveDTOReverseMapper.map(src, dst, mapperContext);
    }
}