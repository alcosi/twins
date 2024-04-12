package org.twins.core.featurer.domain.initiator;

import lombok.RequiredArgsConstructor;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.springframework.stereotype.Component;
import org.twins.core.dao.domain.DomainEntity;

import java.util.Properties;

@Component
@Featurer(id = 2501,
        name = "DomainInitiatorBasic",
        description = "")
@RequiredArgsConstructor
public class DomainInitiatorBasic extends DomainInitiator {
    @Override
    protected void init(Properties properties, DomainEntity domainEntity) throws ServiceException {
        domainEntity
                .setBusinessAccountInitiatorFeaturer(null)
                .setBusinessAccountInitiatorFeaturerId(null)
                .setBusinessAccountInitiatorParams(null)
                .setBusinessAccountTemplateTwinId(null);
    }
}
