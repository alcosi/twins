package org.twins.core.mappers.rest.domain;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.dao.domain.DomainBusinessAccountUserEntity;
import org.twins.core.dto.rest.domain.DomainBusinessAccountUserDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
@RequiredArgsConstructor
public class DomainBusinessAccountUserRestDTOMapper extends RestSimpleDTOMapper<DomainBusinessAccountUserEntity, DomainBusinessAccountUserDTOv1> {

    @Override
    public void map(DomainBusinessAccountUserEntity src, DomainBusinessAccountUserDTOv1 dst, MapperContext mapperContext) throws Exception {
        dst
                .setUserId(src.getUserId())
                .setBusinessAccountId(src.getBusinessAccountId())
                .setLastActivityAt(src.getLastActivityAt() != null ? src.getLastActivityAt().toLocalDateTime() : null)
                .setCreatedAt(src.getCreatedAt() != null ? src.getCreatedAt().toLocalDateTime() : null);
    }
}
