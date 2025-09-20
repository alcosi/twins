package org.twins.core.mappers.rest.domain;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.dao.domain.DomainSubscriptionEventEntity;
import org.twins.core.dto.rest.domain.DomainSubscriptionEventBaseDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
@RequiredArgsConstructor
public class DomainSubscriptionEventBaseRestDTOMapperV1 extends RestSimpleDTOMapper<DomainSubscriptionEventEntity, DomainSubscriptionEventBaseDTOv1> {

    @Override
    public void map(DomainSubscriptionEventEntity src, DomainSubscriptionEventBaseDTOv1 dst, MapperContext mapperContext) {
        dst
                .setId(src.getId())
                .setDomainId(src.getDomainId())
                .setSubscriptionEventTypeId(src.getSubscriptionEventTypeId())
                .setDispatcherFeaturerId(src.getDispatcherFeaturerId())
                .setDispatcherFeaturerParams(src.getDispatcherFeaturerParams());
    }
}
