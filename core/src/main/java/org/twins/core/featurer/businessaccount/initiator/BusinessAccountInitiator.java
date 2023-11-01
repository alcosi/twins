package org.twins.core.featurer.businessaccount.initiator;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.Featurer;
import org.cambium.featurer.annotations.FeaturerType;
import org.springframework.beans.factory.annotation.Autowired;
import org.twins.core.dao.domain.DomainBusinessAccountEntity;
import org.twins.core.dao.domain.DomainBusinessAccountRepository;
import org.twins.core.service.EntitySmartService;

import java.util.HashMap;
import java.util.Properties;


@FeaturerType(id = 11,
        name = "BusinessAccountInitiator",
        description = "")
@Slf4j
public abstract class BusinessAccountInitiator extends Featurer {
    @Autowired
    EntitySmartService entitySmartService;
    @Autowired
    DomainBusinessAccountRepository domainBusinessAccountRepository;
    public void init(HashMap<String, String> initiatorParams, DomainBusinessAccountEntity domainBusinessAccountEntity) throws ServiceException {
        Properties properties = featurerService.extractProperties(this, initiatorParams, new HashMap<>());
        init(properties, domainBusinessAccountEntity);
        entitySmartService.save(domainBusinessAccountEntity, domainBusinessAccountRepository, EntitySmartService.SaveMode.saveAndThrowOnException);
        postInit(properties, domainBusinessAccountEntity);
    }

    protected abstract void init(Properties properties, DomainBusinessAccountEntity domainBusinessAccountEntity) throws ServiceException;

    protected void postInit(Properties properties, DomainBusinessAccountEntity domainBusinessAccountEntity) throws ServiceException {

    }
}
