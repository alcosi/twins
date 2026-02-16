package org.twins.core.featurer.domain.initiator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.service.EntitySmartService;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.twins.core.dao.domain.DomainEntity;
import org.twins.core.dao.domain.TierEntity;
import org.twins.core.enums.twinclass.OwnerType;
import org.twins.core.enums.i18n.I18nType;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinStatusEntity;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.dao.twinflow.TwinflowEntity;
import org.twins.core.dao.twinflow.TwinflowSchemaMapEntity;
import org.twins.core.domain.ApiUser;
import org.twins.core.featurer.FeaturerTwins;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Properties;
import java.util.UUID;

@Component
@Featurer(id = FeaturerTwins.ID_2502,
        name = "B2B",
        description = "")
@RequiredArgsConstructor
@Slf4j
public class DomainInitiatorB2B extends DomainInitiator {
    @Override
    protected void init(Properties properties, DomainEntity domainEntity) throws ServiceException {

    }

    @Override
    public OwnerType getDefaultTwinClassOwnerType() {
        return OwnerType.DOMAIN_BUSINESS_ACCOUNT;
    }

    @Override
    public boolean isSupportedTwinClassOwnerType(OwnerType ownerType) {
        return switch (ownerType) {
            case DOMAIN, DOMAIN_USER, DOMAIN_BUSINESS_ACCOUNT, DOMAIN_BUSINESS_ACCOUNT_USER -> true;
            default -> false;
        };
    }


    @Override
    @Transactional(rollbackFor = Throwable.class)
    protected void postInit(Properties properties, DomainEntity domainEntity) throws ServiceException {
        super.postInit(properties, domainEntity);
        domainEntity
                .setBusinessAccountInitiatorFeaturerId(FeaturerTwins.ID_1101)
                .setBusinessAccountInitiatorParams(null) // 1101 does not need params
                .setBusinessAccountTemplateTwinId(createBusinessAccountTemplateTwin(domainEntity))
                .setDomainUserInitiatorFeaturerId(FeaturerTwins.ID_3402)
                .setDefaultTierId(createDefaultTier(domainEntity));
    }

    private UUID createDefaultTier(DomainEntity domainEntity) throws ServiceException {
        TierEntity tier = new TierEntity();
        tier
                .setDomainId(domainEntity.getId())
                .setName("Free")
                .setDescription("Default tier for [" + domainEntity.getKey() + "] domain.")
                .setCustom(false)
                .setPermissionSchemaId(domainEntity.getPermissionSchemaId())
                .setTwinflowSchemaId(domainEntity.getTwinflowSchemaId())
                .setTwinClassSchemaId(domainEntity.getTwinClassSchemaId())
                .setAttachmentsStorageQuotaCount(0)
                .setAttachmentsStorageQuotaSize(0L)
                .setUserCountQuota(0);
        return entitySmartService.save(tier, tierRepository,  EntitySmartService.SaveMode.saveAndThrowOnException).getId();
    }

    protected UUID createBusinessAccountTemplateTwin(DomainEntity domainEntity) throws ServiceException {
        ApiUser apiUser = authService.getApiUser();
        TwinClassEntity twinClassEntity = new TwinClassEntity()
                .setDomainId(domainEntity.getId())
                .setAbstractt(false)
                .setUniqueName(false)
                .setExtendsTwinClassId(domainEntity.getAncestorTwinClassId())
                .setKey("DOMAIN_BUSINESS_ACCOUNT_FOR_" + domainEntity.getKey().toUpperCase())
                .setOwnerType(OwnerType.DOMAIN_BUSINESS_ACCOUNT)
                .setCreatedAt(Timestamp.from(Instant.now()))
                .setCreatedByUserId(systemEntityService.getUserIdSystem())
                .setAssigneeRequired(false);
        twinClassEntity = entitySmartService.save(twinClassEntity, twinClassRepository, EntitySmartService.SaveMode.saveAndThrowOnException);

        TwinStatusEntity twinStatusEntity = new TwinStatusEntity()
                .setTwinClassId(twinClassEntity.getId())
                .setKey("Active")
                .setNameI18nId(i18nService.createI18nAndDefaultTranslation(I18nType.TWIN_STATUS_NAME,"Active").getId());
        twinStatusEntity = entitySmartService.save(twinStatusEntity, twinStatusRepository, EntitySmartService.SaveMode.saveAndThrowOnException);

        String twinflowName = "Default business account twinflow";
        TwinflowEntity twinflowEntity = new TwinflowEntity()
                .setTwinClassId(twinClassEntity.getId())
                .setNameI18NId(i18nService.createI18nAndDefaultTranslation(I18nType.TWINFLOW_NAME, twinflowName).getId())
                .setDescriptionI18NId(i18nService.createI18nAndDefaultTranslation(I18nType.TWINFLOW_DESCRIPTION, twinflowName).getId())
                .setInitialTwinStatusId(twinStatusEntity.getId())
                .setCreatedAt(Timestamp.from(Instant.now()))
                .setCreatedByUserId(systemEntityService.getUserIdSystem());
        twinflowEntity = entitySmartService.save(twinflowEntity, twinflowRepository, EntitySmartService.SaveMode.saveAndThrowOnException);

        TwinflowSchemaMapEntity twinflowSchemaMapEntity = new TwinflowSchemaMapEntity()
                .setTwinflowSchemaId(domainEntity.getTwinflowSchemaId())
                .setTwinClassId(twinClassEntity.getId())
                .setTwinflowId(twinflowEntity.getId())
                .setTwinflow(twinflowEntity);
        entitySmartService.save(twinflowSchemaMapEntity, twinflowSchemaMapRepository, EntitySmartService.SaveMode.saveAndThrowOnException);

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
