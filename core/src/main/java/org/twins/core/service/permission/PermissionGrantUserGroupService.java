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
import org.twins.core.dao.permission.PermissionGrantUserGroupEntity;
import org.twins.core.dao.permission.PermissionGrantUserGroupRepository;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.user.UserGroupService;
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
public class PermissionGrantUserGroupService extends EntitySecureFindServiceImpl<PermissionGrantUserGroupEntity> {
    @Getter
    private final PermissionGrantUserGroupRepository repository;
    private final AuthService authService;
    private final PermissionSchemaService permissionSchemaService;
    private final PermissionService permissionService;
    private final UserGroupService userGroupService;
    private final UserService userService;

    @Override
    public CrudRepository<PermissionGrantUserGroupEntity, UUID> entityRepository() {
        return repository;
    }

    @Override
    public Function<PermissionGrantUserGroupEntity, UUID> entityGetIdFunction() {
        return PermissionGrantUserGroupEntity::getId;
    }

    @Override
    public boolean isEntityReadDenied(PermissionGrantUserGroupEntity entity, EntitySmartService.ReadPermissionCheckMode readPermissionCheckMode) throws ServiceException {
        DomainEntity domain = authService.getApiUser().getDomain();
        boolean readDenied = !entity.getPermissionSchema().getDomainId().equals(domain.getId());
        if (readDenied) {
            EntitySmartService.entityReadDenied(readPermissionCheckMode, domain.easyLog(EasyLoggable.Level.NORMAL) + " is not allowed in domain[" + domain.easyLog(EasyLoggable.Level.NORMAL));
        }
        return readDenied;
    }

    @Override
    public boolean validateEntity(PermissionGrantUserGroupEntity entity, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
        if (entity.getPermissionSchemaId() == null)
            return logErrorAndReturnFalse(entity.easyLog(EasyLoggable.Level.NORMAL) + " empty permissionSchemaId");
        if (entity.getPermissionId() == null)
            return logErrorAndReturnFalse(entity.easyLog(EasyLoggable.Level.NORMAL) + " empty permissionId");
        if (entity.getUserGroupId() == null)
            return logErrorAndReturnFalse(entity.easyLog(EasyLoggable.Level.NORMAL) + " empty userGroupId");
        switch (entityValidateMode) {
            case beforeSave:
                if (entity.getPermissionSchema() == null || !entity.getPermissionSchema().getId().equals(entity.getPermissionSchemaId()))
                    entity.setPermissionSchema(permissionSchemaService.findEntitySafe(entity.getPermissionSchemaId()));
                if (entity.getPermission() == null || !entity.getPermission().getId().equals(entity.getPermissionId()))
                    entity.setPermission(permissionService.findEntitySafe(entity.getPermissionId()));
                if (entity.getUserGroup() == null || !entity.getUserGroup().getId().equals(entity.getUserGroupId()))
                    entity.setUserGroup(userGroupService.findEntitySafe(entity.getUserGroupId()));
                if (entity.getGrantedByUserId() != null && (entity.getGrantedByUser() == null || !entity.getGrantedByUser().getId().equals(entity.getGrantedByUserId())))
                    entity.setGrantedByUser(userService.findEntitySafe(entity.getGrantedByUserId()));
        }
        return true;
    }

    @Transactional(rollbackFor = Throwable.class)
    public PermissionGrantUserGroupEntity createPermissionGrantUserGroup(PermissionGrantUserGroupEntity createEntity) throws ServiceException {
        createEntity
                .setGrantedByUserId(authService.getApiUser().getUserId())
                .setGrantedAt(Timestamp.from(Instant.now()));
        return saveSafe(createEntity);
    }

    @Transactional(rollbackFor = Throwable.class)
    public PermissionGrantUserGroupEntity updatePermissionGrantUserGroup(PermissionGrantUserGroupEntity updateEntity) throws ServiceException {
        PermissionGrantUserGroupEntity dbEntity = findEntitySafe(updateEntity.getId());
        ChangesHelper changesHelper = new ChangesHelper();
        updateEntityFieldByEntity(updateEntity, dbEntity, PermissionGrantUserGroupEntity::getPermissionSchemaId, PermissionGrantUserGroupEntity::setPermissionSchemaId,
                PermissionGrantUserGroupEntity.Fields.permissionSchemaId, changesHelper);
        updateEntityFieldByEntity(updateEntity, dbEntity, PermissionGrantUserGroupEntity::getPermissionId, PermissionGrantUserGroupEntity::setPermissionId,
                PermissionGrantUserGroupEntity.Fields.permissionId, changesHelper);
        updateEntityFieldByEntity(updateEntity, dbEntity, PermissionGrantUserGroupEntity::getUserGroupId, PermissionGrantUserGroupEntity::setUserGroupId,
                PermissionGrantUserGroupEntity.Fields.userGroupId, changesHelper);
        return updateSafe(dbEntity, changesHelper);
    }

    @Transactional
    public void deleteById(UUID id) throws ServiceException {
        deleteSafe(id);
    }
}
