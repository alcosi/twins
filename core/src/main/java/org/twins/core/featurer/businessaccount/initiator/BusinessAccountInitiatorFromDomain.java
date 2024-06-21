package org.twins.core.featurer.businessaccount.initiator;

import lombok.RequiredArgsConstructor;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.springframework.stereotype.Component;
import org.twins.core.dao.domain.DomainBusinessAccountEntity;
import org.twins.core.featurer.FeaturerTwins;

import java.util.Properties;

@Component
@Featurer(id = FeaturerTwins.ID_1103,
        name = "BusinessAccountInitiatorFromParams",
        description = "")
@RequiredArgsConstructor
public class BusinessAccountInitiatorFromDomain extends BusinessAccountInitiator {
    @Override
    protected void init(Properties properties, DomainBusinessAccountEntity domainBusinessAccountEntity) throws ServiceException {
        //todo perhaps this can be set to nulls
        domainBusinessAccountEntity
                .setPermissionSchemaId(domainBusinessAccountEntity.getDomain().getPermissionSchemaId())
                .setTwinClassSchemaId(domainBusinessAccountEntity.getDomain().getTwinClassSchemaId())
                .setTwinflowSchemaId(domainBusinessAccountEntity.getDomain().getTwinflowSchemaId());
    }
}
