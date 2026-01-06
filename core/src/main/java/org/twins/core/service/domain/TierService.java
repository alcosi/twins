package org.twins.core.service.domain;

import io.github.breninsul.logging.aspect.JavaLoggingLevel;
import io.github.breninsul.logging.aspect.annotation.LogExecutionTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.EasyLoggable;
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
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.permission.PermissionSchemaService;
import org.twins.core.service.twinclass.TwinClassSchemaService;
import org.twins.core.service.twinflow.TwinflowSchemaService;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.UUID;
import java.util.function.Function;

@Slf4j
@Service
@LogExecutionTime(logPrefix = "LONG EXECUTION TIME:", logIfTookMoreThenMs = 2 * 1000, level = JavaLoggingLevel.WARNING)
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
        ApiUser apiUser = authService.getApiUser();
        if (!entity.getDomainId().equals(authService.getApiUser().getDomainId())) {
            EntitySmartService.entityReadDenied(readPermissionCheckMode, entity.logShort() + " is not allows in domain[" + apiUser.getDomainId() + "]");
            return true;
        }
        return false;
    }

    @Override
    public boolean validateEntity(TierEntity entity, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
        if (entity.getPermissionSchemaId() == null)
            return logErrorAndReturnFalse(entity.easyLog(EasyLoggable.Level.NORMAL) + " empty permissionSchemaId");
        if (entity.getTwinflowSchemaId() == null)
            return logErrorAndReturnFalse(entity.easyLog(EasyLoggable.Level.NORMAL) + " empty twinflowSchemaId");
        if (entity.getTwinClassSchemaId() == null)
            return logErrorAndReturnFalse(entity.easyLog(EasyLoggable.Level.NORMAL) + " empty twinClassSchemaId");

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
            return logErrorAndReturnFalse(entity.logShort() + "] is not allows in domain[" + apiUser.getDomainId() + "]");
        return true;
    }

    public UUID checkTierValidForRegistration(UUID tierId) throws ServiceException {
        findEntitySafe(tierId);
        //todo check that tier it active
        return tierId;
    }

    @Transactional(rollbackFor = Throwable.class)
    public TierEntity createTier(TierEntity tierCreate) throws ServiceException {
        tierCreate
                .setDomainId(authService.getApiUser().getDomainId())
                .setCreatedAt(Timestamp.from(Instant.now()));
        return saveSafe(tierCreate);
    }

    @Transactional(rollbackFor = Throwable.class)
    public TierEntity updateTier(TierEntity tierUpdate) throws ServiceException {
        TierEntity dbTierEntity = findEntitySafe(tierUpdate.getId());
        ChangesHelper changesHelper = new ChangesHelper();

        updateEntityFieldByEntity(tierUpdate, dbTierEntity, TierEntity::getName,
                TierEntity::setName, TierEntity.Fields.name, changesHelper);
        updateEntityFieldByEntity(tierUpdate, dbTierEntity, TierEntity::getDescription,
                TierEntity::setDescription, TierEntity.Fields.description, changesHelper);
        updateEntityFieldByEntity(tierUpdate, dbTierEntity, TierEntity::getCustom,
                TierEntity::setCustom, TierEntity.Fields.custom, changesHelper);
        updateEntityFieldByEntity(tierUpdate, dbTierEntity, TierEntity::getPermissionSchemaId,
                TierEntity::setPermissionSchemaId, TierEntity.Fields.permissionSchemaId, changesHelper);
        updateEntityFieldByEntity(tierUpdate, dbTierEntity, TierEntity::getTwinflowSchemaId,
                TierEntity::setTwinflowSchemaId, TierEntity.Fields.twinflowSchemaId, changesHelper);
        updateEntityFieldByEntity(tierUpdate, dbTierEntity, TierEntity::getTwinClassSchemaId,
                TierEntity::setTwinClassSchemaId, TierEntity.Fields.twinClassSchemaId, changesHelper);
        updateEntityFieldByEntity(tierUpdate, dbTierEntity, TierEntity::getAttachmentsStorageQuotaCount,
                TierEntity::setAttachmentsStorageQuotaCount, TierEntity.Fields.attachmentsStorageQuotaCount, changesHelper);
        updateEntityFieldByEntity(tierUpdate, dbTierEntity, TierEntity::getAttachmentsStorageQuotaSize,
                TierEntity::setAttachmentsStorageQuotaSize, TierEntity.Fields.attachmentsStorageQuotaSize, changesHelper);
        updateEntityFieldByEntity(tierUpdate, dbTierEntity, TierEntity::getUserCountQuota,
                TierEntity::setUserCountQuota, TierEntity.Fields.userCountQuota, changesHelper);

        dbTierEntity.setUpdatedAt(Timestamp.from(Instant.now()));

        return updateSafe(dbTierEntity, changesHelper);
    }

    @Transactional
    public void deleteTier(UUID id) throws ServiceException {
        deleteSafe(id);
    }
}
