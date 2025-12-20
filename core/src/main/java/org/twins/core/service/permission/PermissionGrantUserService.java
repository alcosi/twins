package org.twins.core.service.permission;

import io.github.breninsul.logging.aspect.JavaLoggingLevel;
import io.github.breninsul.logging.aspect.annotation.LogExecutionTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
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
import org.twins.core.dao.domain.DomainEntity;
import org.twins.core.dao.permission.PermissionGrantUserEntity;
import org.twins.core.dao.permission.PermissionGrantUserRepository;
import org.twins.core.domain.ApiUser;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.user.UserService;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.UUID;
import java.util.function.Function;

@Slf4j
@Service
@LogExecutionTime(logPrefix = "LONG EXECUTION TIME:", logIfTookMoreThenMs = 2 * 1000, level = JavaLoggingLevel.WARNING)
@Lazy
@AllArgsConstructor
public class PermissionGrantUserService extends EntitySecureFindServiceImpl<PermissionGrantUserEntity> {
    @Getter
    private final PermissionGrantUserRepository permissionGrantUserRepository;
    private final AuthService authService;
    private final PermissionSchemaService permissionSchemaService;
    private final PermissionService permissionService;
    private final UserService userService;

    @Override
    public CrudRepository<PermissionGrantUserEntity, UUID> entityRepository() {
        return permissionGrantUserRepository;
    }

    @Override
    public Function<PermissionGrantUserEntity, UUID> entityGetIdFunction() {
        return PermissionGrantUserEntity::getId;
    }

    @Override
    public boolean isEntityReadDenied(PermissionGrantUserEntity entity, EntitySmartService.ReadPermissionCheckMode readPermissionCheckMode) throws ServiceException {
        DomainEntity domain = authService.getApiUser().getDomain();
        boolean readDenied = !entity.getPermissionSchema().getDomainId().equals(domain.getId());
        if (readDenied) {
            EntitySmartService.entityReadDenied(readPermissionCheckMode, domain.easyLog(EasyLoggable.Level.NORMAL) + " is not allowed in domain[" + domain.easyLog(EasyLoggable.Level.NORMAL));
        }
        return readDenied;
    }

    @Override
    public boolean validateEntity(PermissionGrantUserEntity entity, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
        if (entity.getPermissionSchemaId() == null)
            return logErrorAndReturnFalse(entity.easyLog(EasyLoggable.Level.SHORT) + " empty permissionSchemaId");
        if (entity.getPermissionId() == null)
            return logErrorAndReturnFalse(entity.easyLog(EasyLoggable.Level.SHORT) + " empty permissionId");
        if (entity.getUserId() == null)
            return logErrorAndReturnFalse(entity.easyLog(EasyLoggable.Level.SHORT) + " empty userId");
        if (entity.getGrantedByUserId() == null)
            return logErrorAndReturnFalse(entity.easyLog(EasyLoggable.Level.SHORT) + " empty grantedByUserId");
        switch (entityValidateMode) {
            case beforeSave:
                boolean grantedForUser = permissionGrantUserRepository.existsByPermissionSchemaIdAndPermissionIdAndUserId(entity.getPermissionSchemaId(), entity.getPermissionId(), entity.getUserId());
                if (grantedForUser)
                    throw new ServiceException(ErrorCodeTwins.PERMISSION_GRANT_USER_ALREADY_EXISTS);
                if (entity.getPermissionSchema() == null || !entity.getPermission().getId().equals(entity.getPermissionSchemaId()))
                    entity.setPermissionSchema(permissionSchemaService.findEntitySafe(entity.getPermissionSchemaId()));
                if (entity.getPermission() == null || !entity.getPermission().getId().equals(entity.getPermissionId()))
                    entity.setPermission(permissionService.findEntitySafe(entity.getPermissionId()));
                if (entity.getUser() == null || !entity.getUser().getId().equals(entity.getUserId()))
                    entity.setUser(userService.findEntitySafe(entity.getUserId()));
                if (entity.getGrantedByUser() == null || !entity.getGrantedByUser().getId().equals(entity.getGrantedByUserId()))
                    entity.setGrantedByUser(userService.findEntitySafe(entity.getGrantedByUserId()));
        }
        return true;
    }

    @Transactional(rollbackFor = Throwable.class)
    public PermissionGrantUserEntity createPermissionGrantUser(PermissionGrantUserEntity createEntity) throws ServiceException {
        ApiUser apiUser = authService.getApiUser();
        createEntity
                .setGrantedByUserId(apiUser.getUserId())
                .setDomainId(apiUser.getDomainId())
                .setBusinessAccountId(createEntity.getBusinessAccountId())
                .setGrantedAt(Timestamp.from(Instant.now()));
        return saveSafe(createEntity);
    }

    @Transactional(rollbackFor = Throwable.class)
    public PermissionGrantUserEntity updatePermissionGrantUser(PermissionGrantUserEntity updateEntity) throws ServiceException {
        PermissionGrantUserEntity dbEntity = findEntitySafe(updateEntity.getId());
        ChangesHelper changesHelper = new ChangesHelper();
        updateEntityFieldByEntity(updateEntity, dbEntity, PermissionGrantUserEntity::getPermissionSchemaId, PermissionGrantUserEntity::setPermissionSchemaId,
                PermissionGrantUserEntity.Fields.permissionSchemaId, changesHelper);
        updateEntityFieldByEntity(updateEntity, dbEntity, PermissionGrantUserEntity::getPermissionId, PermissionGrantUserEntity::setPermissionId,
                PermissionGrantUserEntity.Fields.permissionId, changesHelper);
        updateEntityFieldByEntity(updateEntity, dbEntity, PermissionGrantUserEntity::getUserId, PermissionGrantUserEntity::setUserId,
                PermissionGrantUserEntity.Fields.userId, changesHelper);
        return updateSafe(dbEntity, changesHelper);
    }

    @Transactional
    public void deletePermissionGrantUser(UUID id) throws ServiceException {
        deleteSafe(id);
    }
}
