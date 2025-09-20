package org.twins.core.mappers.rest.domain;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModeBinding;
import org.twins.core.controller.rest.annotation.MapperModePointerBinding;
import org.twins.core.dao.domain.DomainSubscriptionEventEntity;
import org.twins.core.dto.rest.domain.DomainSubscriptionEventBaseDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.featurer.FeaturerRestDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.DomainSubscriptionEventMode;
import org.twins.core.mappers.rest.mappercontext.modes.FeaturerMode;

@Component
@RequiredArgsConstructor
@MapperModeBinding(modes = DomainSubscriptionEventMode.class)
public class DomainSubscriptionEventBaseRestDTOMapper extends RestSimpleDTOMapper<DomainSubscriptionEventEntity, DomainSubscriptionEventBaseDTOv1> {

    @MapperModePointerBinding(modes = FeaturerMode.DomainSubscriptionEvent2FeaturerMode.class)
    private final FeaturerRestDTOMapper featurerRestDTOMapper;

    @Override
    public void map(DomainSubscriptionEventEntity src, DomainSubscriptionEventBaseDTOv1 dst, MapperContext mapperContext) throws Exception {
        switch (mapperContext.getModeOrUse(DomainSubscriptionEventMode.SHORT)) {
            case SHORT -> dst
                    .setId(src.getId())
                    .setDomainId(src.getDomainId())
                    .setSubscriptionEventTypeId(src.getSubscriptionEventTypeId());
            case DETAILED -> dst
                    .setId(src.getId())
                    .setDomainId(src.getDomainId())
                    .setSubscriptionEventTypeId(src.getSubscriptionEventTypeId())
                    .setDispatcherFeaturerId(src.getDispatcherFeaturerId())
                    .setDispatcherFeaturerParams(src.getDispatcherFeaturerParams());
        }

        if (mapperContext.hasModeButNot(FeaturerMode.DomainSubscriptionEvent2FeaturerMode.HIDE)) {
            dst
                    .setDispatcherFeaturerId(src.getDispatcherFeaturerId())
                    .setDispatcherFeaturer(featurerRestDTOMapper.convertOrPostpone(src.getDispatcherFeaturer(), mapperContext.forkOnPoint(mapperContext.getModeOrUse(FeaturerMode.DomainSubscriptionEvent2FeaturerMode.SHORT))));
        }
    }
}
