package org.twins.core.mappers.rest.tier;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.dao.domain.TierEntity;
import org.twins.core.dto.rest.tier.TierCreateRqDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.service.auth.AuthService;

@Component
@RequiredArgsConstructor
public class TierCreateDTOReverseMapper extends RestSimpleDTOMapper<TierCreateRqDTOv1, TierEntity> {
    private final TierSaveDTOReverseMapper tierSaveDTOReverseMapper;
    private final AuthService authService;

    @Override
    public void map(TierCreateRqDTOv1 src, TierEntity dst, MapperContext mapperContext) throws Exception {
        tierSaveDTOReverseMapper.map(src, dst, mapperContext);
        dst
                .setId(src.getId())
                .setDomainId(authService.getApiUser().getDomainId());
    }
}