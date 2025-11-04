package org.twins.core.mappers.rest.domain;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.dao.domain.DomainSubscriptionEventEntity;
import org.twins.core.dto.rest.domain.DomainSubscriptionEventSaveRqDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
@RequiredArgsConstructor
public class DomainSubscriptionEventSaveRestDTOReverseMapper extends RestSimpleDTOMapper<DomainSubscriptionEventSaveRqDTOv1, DomainSubscriptionEventEntity> {

    @Override
    public void map(DomainSubscriptionEventSaveRqDTOv1 src, DomainSubscriptionEventEntity dst, MapperContext mapperContext) throws Exception {
        dst
                .setDomainId(src.getDomainId())
                .setSubscriptionEventTypeId(src.getSubscriptionEventTypeId())
                .setDispatcherFeaturerId(src.getDispatcherFeaturerId())
                .setDispatcherFeaturerParams(src.getDispatcherFeaturerParams());
    }
}
