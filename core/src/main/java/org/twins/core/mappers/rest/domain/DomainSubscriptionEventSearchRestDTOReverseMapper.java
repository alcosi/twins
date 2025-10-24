package org.twins.core.mappers.rest.domain;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.domain.search.DomainSubscriptionEventSearch;
import org.twins.core.dto.rest.domain.DomainSubscriptionEventSearchRqDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
@RequiredArgsConstructor
public class DomainSubscriptionEventSearchRestDTOReverseMapper extends RestSimpleDTOMapper<DomainSubscriptionEventSearchRqDTOv1, DomainSubscriptionEventSearch> {

    @Override
    public void map(DomainSubscriptionEventSearchRqDTOv1 src, DomainSubscriptionEventSearch dst, MapperContext mapperContext) throws Exception {
        dst
                .setIdList(src.getIdList())
                .setIdExcludeList(src.getIdExcludeList())
                .setDomainIdList(src.getDomainIdList())
                .setDomainIdExcludeList(src.getDomainIdExcludeList())
                .setSubscriptionEventTypeList(src.getSubscriptionEventTypeList())
                .setSubscriptionEventTypeExcludeList(src.getSubscriptionEventTypeExcludeList())
                .setDispatcherFeaturerIdList(src.getDispatcherFeaturerIdList())
                .setDispatcherFeaturerIdExcludeList(src.getDispatcherFeaturerIdExcludeList());
    }
}
