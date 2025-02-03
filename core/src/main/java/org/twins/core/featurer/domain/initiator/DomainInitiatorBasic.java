package org.twins.core.featurer.domain.initiator;

import lombok.RequiredArgsConstructor;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.springframework.stereotype.Component;
import org.twins.core.dao.domain.DomainEntity;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.featurer.FeaturerTwins;

import java.util.Properties;

@Component
@Featurer(id = FeaturerTwins.ID_2501,
        name = "Basic",
        description = "")
@RequiredArgsConstructor
public class DomainInitiatorBasic extends DomainInitiator {

    @Override
    public TwinClassEntity.OwnerType getDefaultTwinClassOwnerType() {
        return TwinClassEntity.OwnerType.DOMAIN;
    }

    @Override
    public boolean isSupportedTwinClassOwnerType(TwinClassEntity.OwnerType ownerType) {
        return switch (ownerType) {
            case DOMAIN, DOMAIN_USER -> true;
            default -> false;
        };
    }

    @Override
    protected void init(Properties properties, DomainEntity domainEntity) throws ServiceException {
        domainEntity
                .setBusinessAccountInitiatorFeaturer(null)
                .setBusinessAccountInitiatorFeaturerId(null)
                .setBusinessAccountInitiatorParams(null)
                .setBusinessAccountTemplateTwinId(null)
                .setDomainUserTemplateTwinId(null);
    }
}
