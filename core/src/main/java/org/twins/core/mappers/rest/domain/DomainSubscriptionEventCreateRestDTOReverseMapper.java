package org.twins.core.mappers.rest.domain;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.dao.domain.DomainSubscriptionEventEntity;
import org.twins.core.dto.rest.domain.DomainSubscriptionEventCreateRqDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
@RequiredArgsConstructor
public class DomainSubscriptionEventCreateRestDTOReverseMapper extends RestSimpleDTOMapper<DomainSubscriptionEventCreateRqDTOv1, DomainSubscriptionEventEntity> {

    private final DomainSubscriptionEventSaveRestDTOReverseMapper domainSubscriptionEventSaveRestDTOReverseMapper;

    @Override
    public void map(DomainSubscriptionEventCreateRqDTOv1 src, DomainSubscriptionEventEntity dst, MapperContext mapperContext) throws Exception {
        domainSubscriptionEventSaveRestDTOReverseMapper.map(src, dst, mapperContext);
    }
}
