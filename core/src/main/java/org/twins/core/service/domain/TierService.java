package org.twins.core.service.domain;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.util.ChangesHelper;
import org.cambium.service.EntitySecureFindServiceImpl;
import org.cambium.service.EntitySmartService;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.twins.core.dao.domain.TierEntity;
import org.twins.core.dao.domain.TierRepository;
import org.twins.core.domain.ApiUser;
import org.twins.core.domain.tier.TierCreate;
import org.twins.core.domain.tier.TierUpdate;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.permission.PermissionSchemaService;
import org.twins.core.service.twinclass.TwinClassSchemaService;
import org.twins.core.service.twinflow.TwinflowSchemaSearchService;
import org.twins.core.service.twinflow.TwinflowSchemaService;

import java.util.Optional;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Function;

@Slf4j
@Service
@Lazy
@RequiredArgsConstructor
public class TierService extends EntitySecureFindServiceImpl<TierEntity> {
    private final PermissionSchemaService permissionSchemaService;
    private final TwinflowSchemaService twinflowSchemaService;
    private final TwinClassSchemaService twinClassSchemaService;

    private final TierRepository tierRepository;
    @Lazy
    private final AuthService authService;


    @Override
    public CrudRepository<TierEntity, UUID> entityRepository() {
        return tierRepository;
    }

    @Override
    public Function<TierEntity, UUID> entityGetIdFunction() {
        return TierEntity::getId;
    }

    @Override
    public boolean isEntityReadDenied(TierEntity entity, EntitySmartService.ReadPermissionCheckMode readPermissionCheckMode) throws ServiceException {
        return false;
    }

    @Override
    public boolean validateEntity(TierEntity entity, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
        switch (entityValidateMode) {
            case beforeSave:
                if (entity.getPermissionSchema() == null || !entity.getPermissionSchema().getId().equals(entity.getPermissionSchemaId()))
                    entity.setPermissionSchema(permissionSchemaService.findEntitySafe(entity.getPermissionSchemaId()));
                if (entity.getTwinflowSchema() == null || !entity.getTwinflowSchema().getId().equals(entity.getTwinflowSchemaId()))
                    entity.setTwinflowSchema(twinflowSchemaService.findEntitySafe(entity.getTwinflowSchemaId()));
                if (entity.getTwinClassSchema() == null || !entity.getTwinClassSchema().getId().equals(entity.getTwinClassSchemaId()))
                    entity.setTwinClassSchema(twinClassSchemaService.findEntitySafe(entity.getTwinClassSchemaId()));
        }
        ApiUser apiUser = authService.getApiUser();
        if (!entity.getDomainId().equals(apiUser.getDomainId()))
            return logErrorAndReturnFalse("domainTierId[" + entity.getId() + "] is not allows in domain[" + apiUser.getDomainId() + "]");
        return true;
    }

    public UUID checkTierAllowed(UUID domainTierId) throws ServiceException {
        Optional<TierEntity> domainBusinessAccountTierEntity = tierRepository.findById(domainTierId);
        if (domainBusinessAccountTierEntity.isEmpty())
            throw new ServiceException(ErrorCodeTwins.UUID_UNKNOWN, "unknown domainTierId[" + domainTierId + "]");
        validateEntityAndThrow(domainBusinessAccountTierEntity.get(), EntitySmartService.EntityValidateMode.beforeSave);
        return domainTierId;
    }

    @Transactional(rollbackFor = Throwable.class)
    public TierEntity createTier(TierCreate tierCreate) throws ServiceException {
        TierEntity tier = new TierEntity()
                .setId(tierCreate.getId())
                .setDomainId(tierCreate.getDomainId())
                .setName(tierCreate.getName())
                .setCustom(tierCreate.isCustom())
                .setPermissionSchemaId(tierCreate.getPermissionSchemaId())
                .setTwinflowSchemaId(tierCreate.getTwinflowSchemaId())
                .setTwinClassSchemaId(tierCreate.getTwinClassSchemaId())
                .setAttachmentsStorageQuotaCount(tierCreate.getAttachmentsStorageQuotaCount())
                .setAttachmentsStorageQuotaSize(tierCreate.getAttachmentsStorageQuotaSize())
                .setUserCountQuota(tierCreate.getUserCountQuota())
                .setDescription(tierCreate.getDescription());
        validateEntityAndThrow(tier, EntitySmartService.EntityValidateMode.beforeSave);
        return tierRepository.save(tier);
    }

    @Transactional(rollbackFor = Throwable.class)
    public TierEntity updateTier(TierUpdate tierUpdate) throws ServiceException {
        TierEntity dbTierEntity = findEntitySafe(tierUpdate.getId());
        ChangesHelper changesHelper = new ChangesHelper();

        updateTierName(dbTierEntity, tierUpdate.getName(), changesHelper);
        updateTierDescription(dbTierEntity, tierUpdate.getDescription(), changesHelper);
        updateTierCustom(dbTierEntity, tierUpdate.isCustom(), changesHelper);
        updateTierPermissionSchemaId(dbTierEntity, tierUpdate.getPermissionSchemaId(), changesHelper);
        updateTierTwinflowSchemaId(dbTierEntity, tierUpdate.getTwinflowSchemaId(), changesHelper);
        updateTierTwinClassSchemaId(dbTierEntity, tierUpdate.getTwinClassSchemaId(), changesHelper);
        updateTierAttachmentsStorageQuotaCount(dbTierEntity, tierUpdate.getAttachmentsStorageQuotaCount(), changesHelper);
        updateTierAttachmentsStorageQuotaSize(dbTierEntity, tierUpdate.getAttachmentsStorageQuotaSize(), changesHelper);
        updateTierUserCountQuota(dbTierEntity, tierUpdate.getUserCountQuota(), changesHelper);

        validateEntity(dbTierEntity, EntitySmartService.EntityValidateMode.beforeSave);

        if (changesHelper.hasChanges()) {
            dbTierEntity = entitySmartService.saveAndLogChanges(dbTierEntity, tierRepository, changesHelper);
        }

        return dbTierEntity;
    }

    private void updateTierName(TierEntity tierEntity, String newName, ChangesHelper changesHelper) {
        if (!newName.isEmpty() && !newName.equals(tierEntity.getName())) {
            changesHelper.add(TierEntity.Fields.name, tierEntity.getName(), newName);
            tierEntity.setName(newName);
        }
    }

    private void updateTierDescription(TierEntity tierEntity, String newDescription, ChangesHelper changesHelper) {
        if (!newDescription.isEmpty() && !newDescription.equals(tierEntity.getDescription())) {
            changesHelper.add(TierEntity.Fields.description, tierEntity.getDescription(), newDescription);
            tierEntity.setDescription(newDescription);
        }
    }

    private void updateTierCustom(TierEntity tierEntity, Boolean custom, ChangesHelper changesHelper) {
        if (custom != null && !custom.equals(tierEntity.isCustom())) {
            changesHelper.add(TierEntity.Fields.custom, tierEntity.isCustom(), custom);
            tierEntity.setCustom(custom);
        }
    }

    private void updateTierPermissionSchemaId(TierEntity tierEntity, UUID permissionSchemaId, ChangesHelper changesHelper) {
        if (permissionSchemaId != null && !permissionSchemaId.equals(tierEntity.getPermissionSchemaId())) {
            changesHelper.add(TierEntity.Fields.permissionSchemaId, tierEntity.getPermissionSchemaId(), permissionSchemaId);
            tierEntity.setPermissionSchemaId(permissionSchemaId);
        }
    }

    private void updateTierTwinflowSchemaId(TierEntity tierEntity, UUID twinflowSchemaId, ChangesHelper changesHelper) {
        if (twinflowSchemaId != null && !twinflowSchemaId.equals(tierEntity.getTwinflowSchemaId())) {
            changesHelper.add(TierEntity.Fields.twinflowSchemaId, tierEntity.getTwinflowSchemaId(), twinflowSchemaId);
            tierEntity.setTwinflowSchemaId(twinflowSchemaId);
        }
    }

    private void updateTierTwinClassSchemaId(TierEntity tierEntity, UUID twinClassSchemaId, ChangesHelper changesHelper) {
        if (twinClassSchemaId != null && !twinClassSchemaId.equals(tierEntity.getTwinClassSchemaId())) {
            changesHelper.add(TierEntity.Fields.twinClassSchemaId, tierEntity.getTwinClassSchemaId(), twinClassSchemaId);
            tierEntity.setTwinClassSchemaId(twinClassSchemaId);
        }
    }

    private void updateTierAttachmentsStorageQuotaCount(TierEntity tierEntity, Integer quotaCount, ChangesHelper changesHelper) {
        if (quotaCount != null && !quotaCount.equals(tierEntity.getAttachmentsStorageQuotaCount())) {
            changesHelper.add(TierEntity.Fields.attachmentsStorageQuotaCount, tierEntity.getAttachmentsStorageQuotaCount(), quotaCount);
            tierEntity.setAttachmentsStorageQuotaCount(quotaCount);
        }
    }

    private void updateTierAttachmentsStorageQuotaSize(TierEntity tierEntity, Long quotaSize, ChangesHelper changesHelper) {
        if (quotaSize != null && !quotaSize.equals(tierEntity.getAttachmentsStorageQuotaSize())) {
            changesHelper.add(TierEntity.Fields.attachmentsStorageQuotaSize, tierEntity.getAttachmentsStorageQuotaSize(), quotaSize);
            tierEntity.setAttachmentsStorageQuotaSize(quotaSize);
        }
    }

    private void updateTierUserCountQuota(TierEntity tierEntity, Integer userCountQuota, ChangesHelper changesHelper) {
        if (userCountQuota != null && !userCountQuota.equals(tierEntity.getUserCountQuota())) {
            changesHelper.add(TierEntity.Fields.userCountQuota, tierEntity.getUserCountQuota(), userCountQuota);
            tierEntity.setUserCountQuota(userCountQuota);
        }
    }
}
