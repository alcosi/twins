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
        TierEntity tierEntity = findEntitySafe(tierUpdate.getId());
        ChangesHelper changesHelper = new ChangesHelper();

        if (changesHelper.hasChanges()) {
            validateEntityAndThrow(tierEntity, EntitySmartService.EntityValidateMode.beforeSave);
            entitySmartService.saveAndLogChanges(tierEntity, tierRepository, changesHelper);
        }
        return tierEntity;
    }

}
