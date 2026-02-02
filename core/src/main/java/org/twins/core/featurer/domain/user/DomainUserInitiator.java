package org.twins.core.featurer.domain.user;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.FeaturerType;
import org.cambium.service.EntitySmartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.transaction.annotation.Transactional;
import org.twins.core.dao.domain.DomainUserEntity;
import org.twins.core.dao.domain.DomainUserRepository;
import org.twins.core.domain.twinoperation.TwinDuplicate;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.service.twin.TwinService;

import java.util.HashMap;
import java.util.Properties;


@FeaturerType(id = FeaturerTwins.TYPE_34,
        name = "DomainUserInitiator",
        description = "")
@Slf4j
public abstract class DomainUserInitiator extends FeaturerTwins {
    @Autowired
    private EntitySmartService entitySmartService;
    @Autowired
    private DomainUserRepository domainUserRepository;
    @Lazy
    @Autowired
    private TwinService twinService;

    @Transactional(rollbackFor = Throwable.class)
    public void init(HashMap<String, String> initiatorParams, DomainUserEntity domainUserEntity) throws ServiceException {
        Properties properties = featurerService.extractProperties(this, initiatorParams);
        init(properties, domainUserEntity);
        domainUserEntity = entitySmartService.save(domainUserEntity, domainUserRepository, EntitySmartService.SaveMode.saveAndThrowOnException);
        postInit(properties, domainUserEntity);
    }

    protected abstract void init(Properties properties, DomainUserEntity domainUserEntity) throws ServiceException;


    protected void postInit(Properties properties, DomainUserEntity domainUserEntity) throws ServiceException {
        if (domainUserEntity.getDomain().getDomainUserTemplateTwinId() != null) {
            TwinDuplicate duplicateTwin = twinService.createDuplicateTwin(domainUserEntity.getDomain().getDomainUserTemplateTwinId(), domainUserEntity.getId());
            duplicateTwin.getDuplicate().setHeadTwinId(domainUserEntity.getUserId()); //user twin was created before
            twinService.saveDuplicateTwin(duplicateTwin);
        }
    }
}
