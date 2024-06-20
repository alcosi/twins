package org.twins.core.featurer.businessaccount.initiator;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.FeaturerType;
import org.cambium.service.EntitySmartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.twins.core.dao.domain.DomainBusinessAccountEntity;
import org.twins.core.dao.domain.DomainBusinessAccountRepository;
import org.twins.core.domain.ApiUser;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.service.SystemEntityService;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.twin.TwinService;

import java.util.HashMap;
import java.util.Properties;


@FeaturerType(id = FeaturerTwins.TYPE_11,
        name = "BusinessAccountInitiator",
        description = "")
@Slf4j
public abstract class BusinessAccountInitiator extends FeaturerTwins {
    @Autowired
    EntitySmartService entitySmartService;
    @Autowired
    DomainBusinessAccountRepository domainBusinessAccountRepository;
    @Lazy
    @Autowired
    TwinService twinService;
    @Lazy
    @Autowired
    AuthService authService;
    @Lazy
    @Autowired
    SystemEntityService systemEntityService;

    public void init(HashMap<String, String> initiatorParams, DomainBusinessAccountEntity domainBusinessAccountEntity) throws ServiceException {
        Properties properties = featurerService.extractProperties(this, initiatorParams, new HashMap<>());
        init(properties, domainBusinessAccountEntity);
        domainBusinessAccountEntity = entitySmartService.save(domainBusinessAccountEntity, domainBusinessAccountRepository, EntitySmartService.SaveMode.saveAndThrowOnException);
        postInit(properties, domainBusinessAccountEntity);
    }

    protected abstract void init(Properties properties, DomainBusinessAccountEntity domainBusinessAccountEntity) throws ServiceException;

    protected void postInit(Properties properties, DomainBusinessAccountEntity domainBusinessAccountEntity) throws ServiceException {
        ApiUser apiUser = authService.getApiUser();
        twinService.duplicateTwin(domainBusinessAccountEntity.getDomain().getBusinessAccountTemplateTwinId(), domainBusinessAccountEntity.getBusinessAccount(), apiUser.getUser(), domainBusinessAccountEntity.getId()); //creating twin for business account in domain
    }
}
