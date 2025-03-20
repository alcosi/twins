package org.twins.core.mappers.rest.domain;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.dao.domain.DomainEntity;
import org.twins.core.dto.rest.domain.DomainCreateDTOv1;
import org.twins.core.dto.rest.domain.DomainUpdateDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;


@Component
@RequiredArgsConstructor
public class DomainUpdateRestDTOReverseMapper extends RestSimpleDTOMapper<DomainUpdateDTOv1, DomainEntity> {
    private final DomainSaveRestDTOReverseMapper domainSaveRestDTOReverseMapper;

    @Override
    public void map(DomainUpdateDTOv1 src, DomainEntity dst, MapperContext mapperContext) throws Exception {
        domainSaveRestDTOReverseMapper.map(src, dst, mapperContext);
        dst.setId(src.getId());
    }
}
