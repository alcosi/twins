package org.twins.core.featurer.domain.initiator;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.FeaturerType;
import org.cambium.service.EntitySmartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.transaction.annotation.Transactional;
import org.twins.core.dao.domain.DomainEntity;
import org.twins.core.dao.domain.DomainRepository;
import org.twins.core.dao.domain.DomainTypeEntity;
import org.twins.core.dao.domain.TierRepository;
import org.twins.core.dao.i18n.I18nType;
import org.twins.core.dao.permission.PermissionSchemaEntity;
import org.twins.core.dao.permission.PermissionSchemaRepository;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinRepository;
import org.twins.core.dao.twin.TwinStatusEntity;
import org.twins.core.dao.twin.TwinStatusRepository;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.dao.twinclass.TwinClassRepository;
import org.twins.core.dao.twinclass.TwinClassSchemaEntity;
import org.twins.core.dao.twinclass.TwinClassSchemaRepository;
import org.twins.core.dao.twinflow.*;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.service.SystemEntityService;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.domain.DomainService;
import org.twins.core.service.i18n.I18nService;
import org.twins.core.service.twin.TwinService;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.HashMap;
import java.util.Locale;
import java.util.Properties;
import java.util.UUID;

import static org.twins.core.service.SystemEntityService.TWIN_CLASS_USER;


@FeaturerType(id = FeaturerTwins.TYPE_25,
        name = "DomainInitiator",
        description = "")
@Slf4j
public abstract class DomainInitiator extends FeaturerTwins {
    @Autowired
    EntitySmartService entitySmartService;
    @Autowired
    DomainRepository domainRepository;
    @Autowired
    TwinRepository twinRepository;
    @Autowired
    TwinClassRepository twinClassRepository;
    @Autowired
    TwinStatusRepository twinStatusRepository;
    @Autowired
    TwinflowRepository twinflowRepository;
    @Autowired
    TwinflowSchemaMapRepository twinflowSchemaMapRepository;
    @Autowired
    TwinflowSchemaRepository twinflowSchemaRepository;
    @Autowired
    TwinClassSchemaRepository twinClassSchemaRepository;
    @Autowired
    PermissionSchemaRepository permissionSchemaRepository;
    @Autowired
    TierRepository tierRepository;

    @Lazy
    @Autowired
    TwinService twinService;

    @Lazy
    @Autowired
    AuthService authService;

    @Lazy
    @Autowired
    I18nService i18nService;

    @Lazy
    @Autowired
    DomainService domainService;

    @Lazy
    @Autowired
    SystemEntityService systemEntityService;

    @Transactional(rollbackFor = Throwable.class)
    public DomainEntity init(DomainEntity domainEntity) throws ServiceException {
        DomainTypeEntity domainTypeEntity = domainService.loadDomainType(domainEntity);
        Properties properties = featurerService.extractProperties(this, domainTypeEntity.getDomainInitiatorParams(), new HashMap<>());
        domainEntity
                .setCreatedAt(Timestamp.from(Instant.now()))
                .setTokenHandlerFeaturerId(domainTypeEntity.getDefaultTokenHandlerFeaturer().getId())
                .setTokenHandlerParams(domainTypeEntity.getDefaultTokenHandlerParams())
                .setUserGroupManagerFeaturerId(domainTypeEntity.getDefaultUserGroupManagerFeaturer().getId())
                .setUserGroupManagerFeaturer(domainTypeEntity.getDefaultUserGroupManagerFeaturer())
                .setUserGroupManagerParams(domainTypeEntity.getDefaultUserGroupManagerParams())
                .setAttachmentsStorageUsedSize(0L)
                .setAttachmentsStorageUsedCount(0L);
        if (domainEntity.getDefaultI18nLocaleId() == null)
            domainEntity.setDefaultI18nLocaleId(Locale.ENGLISH); //todo get from I18nService

        init(properties, domainEntity);
        domainEntity = entitySmartService.save(domainEntity, domainRepository, EntitySmartService.SaveMode.saveAndThrowOnException);
        postInit(properties, domainEntity);
        return entitySmartService.save(domainEntity, domainRepository, EntitySmartService.SaveMode.saveAndThrowOnException);
    }

    protected abstract void init(Properties properties, DomainEntity domainEntity) throws ServiceException;

    public abstract TwinClassEntity.OwnerType getDefaultTwinClassOwnerType();
    public abstract boolean isSupportedTwinClassOwnerType(TwinClassEntity.OwnerType ownerType);

    @Transactional(rollbackFor = Throwable.class)
    protected void postInit(Properties properties, DomainEntity domainEntity) throws ServiceException {
        //todo create twin for domain
        domainEntity
                .setAncestorTwinClassId(createAncestorTwinClass(domainEntity))
                .setTwinflowSchemaId(createDefaultTwinflowSchema(domainEntity))
                .setTwinClassSchemaId(createDefaultTwinClassSchema(domainEntity))
                .setPermissionSchemaId(createDefaultPermissionsSchema(domainEntity))
                .setDomainUserTemplateTwinId(createDomainUserTemplateTwin(domainEntity));
    }



    protected UUID createDomainUserTemplateTwin(DomainEntity domainEntity) throws ServiceException {
        TwinClassEntity twinClassEntity = new TwinClassEntity()
                .setDomainId(domainEntity.getId())
                .setAbstractt(false)
                .setKey("DOMAIN_USER_FOR_" + domainEntity.getKey().toUpperCase())
                .setHeadTwinClassId(TWIN_CLASS_USER)
                .setOwnerType(TwinClassEntity.OwnerType.DOMAIN_USER)
                .setCreatedAt(Timestamp.from(Instant.now()))
                .setCreatedByUserId(systemEntityService.getUserIdSystem());
        twinClassEntity = entitySmartService.save(twinClassEntity, twinClassRepository, EntitySmartService.SaveMode.saveAndThrowOnException);

        TwinStatusEntity twinStatusEntity = new TwinStatusEntity()
                .setTwinClassId(twinClassEntity.getId())
                .setTwinClass(twinClassEntity)
                .setKey("Active")
                .setNameI18nId(i18nService.createI18nAndDefaultTranslation(I18nType.TWIN_STATUS_NAME,"Active").getId());
        twinStatusEntity = entitySmartService.save(twinStatusEntity, twinStatusRepository, EntitySmartService.SaveMode.saveAndThrowOnException);

        String twinflowName = "Default domain user twinflow";
        TwinflowEntity twinflowEntity = new TwinflowEntity()
                .setTwinClassId(twinClassEntity.getId())
                .setNameI18NId(i18nService.createI18nAndDefaultTranslation(I18nType.TWINFLOW_NAME, twinflowName).getId())
                .setDescriptionI18NId(i18nService.createI18nAndDefaultTranslation(I18nType.TWINFLOW_DESCRIPTION, twinflowName).getId())
                .setInitialTwinStatusId(twinStatusEntity.getId())
                .setInitialTwinStatus(twinStatusEntity)
                .setCreatedAt(Timestamp.from(Instant.now()))
                .setCreatedByUserId(systemEntityService.getUserIdSystem());
        twinflowEntity = entitySmartService.save(twinflowEntity, twinflowRepository, EntitySmartService.SaveMode.saveAndThrowOnException);

        TwinflowSchemaMapEntity twinflowSchemaMapEntity = new TwinflowSchemaMapEntity()
                .setTwinflowSchemaId(domainEntity.getTwinflowSchemaId())
                .setTwinClassId(twinClassEntity.getId())
                .setTwinClass(twinClassEntity)
                .setTwinflowId(twinflowEntity.getId())
                .setTwinflow(twinflowEntity);
        entitySmartService.save(twinflowSchemaMapEntity, twinflowSchemaMapRepository, EntitySmartService.SaveMode.saveAndThrowOnException);

        TwinEntity twinEntity = new TwinEntity()
                .setTwinClassId(twinClassEntity.getId())
                .setTwinClass(twinClassEntity)
                .setTwinStatusId(twinStatusEntity.getId())
                .setTwinStatus(twinStatusEntity)
                .setName("Domain user template")
                .setCreatedAt(Timestamp.from(Instant.now()))
                .setCreatedByUserId(systemEntityService.getUserIdSystem());
        twinEntity = entitySmartService.save(twinEntity, twinRepository, EntitySmartService.SaveMode.saveAndThrowOnException);
        return twinEntity.getId();
    }

    @Transactional
    protected UUID createAncestorTwinClass(DomainEntity domainEntity) throws ServiceException {
        TwinClassEntity twinClassEntity = new TwinClassEntity()
                .setDomainId(domainEntity.getId())
                .setAbstractt(true)
                .setKey(domainEntity.getKey().toUpperCase())
                .setOwnerType(TwinClassEntity.OwnerType.DOMAIN)
                .setCreatedAt(Timestamp.from(Instant.now()))
                .setExtendsTwinClassId(SystemEntityService.TWIN_CLASS_GLOBAL_ANCESTOR)
                .setCreatedByUserId(systemEntityService.getUserIdSystem());
        twinClassEntity = entitySmartService.save(twinClassEntity, twinClassRepository, EntitySmartService.SaveMode.saveAndThrowOnException);
        return twinClassEntity.getId();
    }

    @Transactional
    protected UUID createDefaultTwinflowSchema(DomainEntity domainEntity) throws ServiceException {
        TwinflowSchemaEntity twinflowSchemaEntity = new TwinflowSchemaEntity()
                .setDomainId(domainEntity.getId())
                .setName("Default domain twinflow schema")
                .setCreatedAt(Timestamp.from(Instant.now()))
                .setCreatedByUserId(systemEntityService.getUserIdSystem());
        twinflowSchemaEntity = entitySmartService.save(twinflowSchemaEntity, twinflowSchemaRepository, EntitySmartService.SaveMode.saveAndThrowOnException);
        return twinflowSchemaEntity.getId();
    }

    @Transactional
    protected UUID createDefaultTwinClassSchema(DomainEntity domainEntity) throws ServiceException {
        TwinClassSchemaEntity twinClassSchemaEntity = new TwinClassSchemaEntity()
                .setDomainId(domainEntity.getId())
                .setName("Default domain twin class schema")
                .setCreatedAt(Timestamp.from(Instant.now()))
                .setCreatedByUserId(systemEntityService.getUserIdSystem());
        twinClassSchemaEntity = entitySmartService.save(twinClassSchemaEntity, twinClassSchemaRepository, EntitySmartService.SaveMode.saveAndThrowOnException);
        return twinClassSchemaEntity.getId();
    }

    @Transactional
    protected UUID createDefaultPermissionsSchema(DomainEntity domainEntity) throws ServiceException {
        PermissionSchemaEntity permissionSchema = new PermissionSchemaEntity()
                .setDomainId(domainEntity.getId())
                .setName("Default domain permission schema")
                .setCreatedAt(Timestamp.from(Instant.now()))
                .setCreatedByUserId(systemEntityService.getUserIdSystem());
        permissionSchema = entitySmartService.save(permissionSchema, permissionSchemaRepository, EntitySmartService.SaveMode.saveAndThrowOnException);
        return permissionSchema.getId();
    }
}
