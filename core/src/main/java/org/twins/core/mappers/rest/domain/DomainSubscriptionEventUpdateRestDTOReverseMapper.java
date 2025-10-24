package org.twins.core.mappers.rest.domain;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.dao.domain.DomainSubscriptionEventEntity;
import org.twins.core.dto.rest.domain.DomainSubscriptionEventUpdateRqDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
@RequiredArgsConstructor
public class DomainSubscriptionEventUpdateRestDTOReverseMapper extends RestSimpleDTOMapper<DomainSubscriptionEventUpdateRqDTOv1, DomainSubscriptionEventEntity> {

    private final DomainSubscriptionEventSaveRestDTOReverseMapper domainSubscriptionEventSaveRestDTOReverseMapper;

    @Override
    public void map(DomainSubscriptionEventUpdateRqDTOv1 src, DomainSubscriptionEventEntity dst, MapperContext mapperContext) throws Exception {
        domainSubscriptionEventSaveRestDTOReverseMapper.map(src, dst, mapperContext);
    }
}

