package org.twins.core.featurer.domain.initiator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.i18n.dao.I18nType;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.twins.core.dao.domain.DomainEntity;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinStatusEntity;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.dao.twinflow.TwinflowEntity;
import org.twins.core.dao.twinflow.TwinflowSchemaMapEntity;
import org.twins.core.service.EntitySmartService;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Properties;
import java.util.UUID;

@Component
@Featurer(id = 2502,
        name = "DomainInitiatorB2B",
        description = "")
@RequiredArgsConstructor
@Slf4j
public class DomainInitiatorB2B extends DomainInitiator {
    @Override
    protected void init(Properties properties, DomainEntity domainEntity) throws ServiceException {

    }

    @Transactional
    @Override
    protected void postInit(Properties properties, DomainEntity domainEntity) throws ServiceException {
        super.postInit(properties, domainEntity);
        domainEntity
                .setBusinessAccountInitiatorFeaturerId(1103)
                .setBusinessAccountInitiatorParams(null) // 1103 does not need params
                .setBusinessAccountTemplateTwinId(createBusinessAccountTemplateTwin(domainEntity));
    }

    @Transactional
    protected UUID createBusinessAccountTemplateTwin(DomainEntity domainEntity) throws ServiceException {
        TwinClassEntity twinClassEntity = new TwinClassEntity()
                .setDomainId(domainEntity.getId())
                .setAbstractt(false)
                .setExtendsTwinClassId(domainEntity.getAncestorTwinClassId())
                .setKey("DOMAIN_BUSINESS_ACCOUNT_FOR_" + domainEntity.getKey().toUpperCase())
                .setOwnerType(TwinClassEntity.OwnerType.DOMAIN_BUSINESS_ACCOUNT)
                .setCreatedAt(Timestamp.from(Instant.now()))
                .setCreatedByUserId(systemEntityService.getUserIdSystem());
        twinClassEntity = entitySmartService.save(twinClassEntity, twinClassRepository, EntitySmartService.SaveMode.saveAndThrowOnException);

        TwinStatusEntity twinStatusEntity = new TwinStatusEntity()
                .setTwinClassId(twinClassEntity.getId())
                .setKey("Active")
                .setNameI18nId(i18nService.createI18nAndDefaultTranslation(I18nType.TWIN_STATUS_NAME, "Active").getI18nId());
        twinStatusEntity = entitySmartService.save(twinStatusEntity, twinStatusRepository, EntitySmartService.SaveMode.saveAndThrowOnException);

        TwinflowEntity twinflowEntity = new TwinflowEntity()
                .setTwinClassId(twinClassEntity.getId())
                .setName("Default business account twinflow")
                .setInitialTwinStatusId(twinStatusEntity.getId())
                .setCreatedAt(Timestamp.from(Instant.now()))
                .setCreatedByUserId(systemEntityService.getUserIdSystem());
        twinflowEntity = entitySmartService.save(twinflowEntity, twinflowRepository, EntitySmartService.SaveMode.saveAndThrowOnException);

        TwinflowSchemaMapEntity twinflowSchemaMapEntity = new TwinflowSchemaMapEntity()
                .setTwinflowSchemaId(domainEntity.getTwinflowSchemaId())
                .setTwinClassId(twinClassEntity.getId())
                .setTwinflowId(twinflowEntity.getId())
                .setTwinflow(twinflowEntity);
        twinflowSchemaMapEntity   = entitySmartService.save(twinflowSchemaMapEntity, twinflowSchemaMapRepository, EntitySmartService.SaveMode.saveAndThrowOnException);

        TwinEntity twinEntity = new TwinEntity()
                .setTwinClassId(twinClassEntity.getId())
                .setTwinStatusId(twinStatusEntity.getId())
                .setName("Business account template")
                .setCreatedAt(Timestamp.from(Instant.now()))
                .setCreatedByUserId(systemEntityService.getUserIdSystem());
        twinEntity = entitySmartService.save(twinEntity, twinRepository, EntitySmartService.SaveMode.saveAndThrowOnException);
        return twinEntity.getId();
    }
}
