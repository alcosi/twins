package org.twins.core.featurer.businessaccount.initiator;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.FeaturerType;
import org.cambium.service.EntitySmartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.twins.core.dao.domain.DomainBusinessAccountEntity;
import org.twins.core.dao.domain.DomainBusinessAccountRepository;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.service.twin.TwinService;

import java.util.HashMap;
import java.util.Properties;


@FeaturerType(id = FeaturerTwins.TYPE_11,
        name = "BusinessAccountInitiator",
        description = "")
@Slf4j
public abstract class BusinessAccountInitiator extends FeaturerTwins {
    @Autowired
    private EntitySmartService entitySmartService;
    @Autowired
    private DomainBusinessAccountRepository domainBusinessAccountRepository;
    @Lazy
    @Autowired
    private TwinService twinService;

    public void init(HashMap<String, String> initiatorParams, DomainBusinessAccountEntity domainBusinessAccountEntity) throws ServiceException {
        Properties properties = featurerService.extractProperties(this, initiatorParams);
        domainBusinessAccountEntity
                .setPermissionSchemaId(domainBusinessAccountEntity.getTier().getPermissionSchemaId())
                .setPermissionSchema(domainBusinessAccountEntity.getTier().getPermissionSchema())
                .setTwinClassSchemaId(domainBusinessAccountEntity.getTier().getTwinClassSchemaId())
                .setTwinflowSchemaId(domainBusinessAccountEntity.getTier().getTwinflowSchemaId())
                .setNotificationSchemaId(domainBusinessAccountEntity.getTier().getNotificationSchemaId())
                .setAttachmentsStorageUsedCount(0L)
                .setAttachmentsStorageUsedSize(0L);

        init(properties, domainBusinessAccountEntity);
        domainBusinessAccountEntity = entitySmartService.save(domainBusinessAccountEntity, domainBusinessAccountRepository, EntitySmartService.SaveMode.saveAndThrowOnException);
        postInit(properties, domainBusinessAccountEntity);
    }

    protected abstract void init(Properties properties, DomainBusinessAccountEntity domainBusinessAccountEntity) throws ServiceException;

    protected void postInit(Properties properties, DomainBusinessAccountEntity domainBusinessAccountEntity) throws ServiceException {
        if (domainBusinessAccountEntity.getDomain().getBusinessAccountTemplateTwinId() != null) {
            twinService.duplicateTwin(domainBusinessAccountEntity.getDomain().getBusinessAccountTemplateTwinId(), domainBusinessAccountEntity.getId()); //creating twin for business account in domain
        }
    }
}
