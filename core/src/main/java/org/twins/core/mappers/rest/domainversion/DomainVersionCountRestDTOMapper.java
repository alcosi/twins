package org.twins.core.mappers.rest.domainversion;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.dao.domain.DomainVersionEntity;
import org.twins.core.domain.CountResult;
import org.twins.core.dto.rest.domainversion.DomainVersionCountDTOv1;
import org.twins.core.enums.sort.DomainVersionGroupField;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
@RequiredArgsConstructor
public class DomainVersionCountRestDTOMapper
        extends RestSimpleDTOMapper<CountResult<DomainVersionEntity, DomainVersionGroupField>, DomainVersionCountDTOv1> {

    @Override
    public void map(CountResult<DomainVersionEntity, DomainVersionGroupField> src, DomainVersionCountDTOv1 dst, MapperContext mapperContext) throws Exception {
        var entity = src.getEntity();
        if (entity == null) {
            dst.setCount(src.getCount());
            return;
        }
        dst
                .setStatus(entity.getStatus())
                .setCount(src.getCount());
    }
}
