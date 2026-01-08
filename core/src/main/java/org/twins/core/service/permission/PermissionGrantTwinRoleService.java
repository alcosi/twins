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
import org.twins.core.dao.permission.PermissionGrantTwinRoleEntity;
import org.twins.core.dao.permission.PermissionGrantTwinRoleRepository;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.twinclass.TwinClassService;
import org.twins.core.service.user.UserService;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.UUID;
import java.util.function.Function;

@Slf4j
@Service
@Lazy
@LogExecutionTime(logPrefix = "LONG EXECUTION TIME:", logIfTookMoreThenMs = 2 * 1000, level = JavaLoggingLevel.WARNING)
@AllArgsConstructor
public class PermissionGrantTwinRoleService extends EntitySecureFindServiceImpl<PermissionGrantTwinRoleEntity> {
    @Getter
    private final PermissionGrantTwinRoleRepository repository;
    private final AuthService authService;
    private final PermissionSchemaService permissionSchemaService;
    private final PermissionService permissionService;
    private final TwinClassService twinClassService;
    private final UserService userService;

    @Override
    public CrudRepository<PermissionGrantTwinRoleEntity, UUID> entityRepository() {
        return repository;
    }

    @Override
    public Function<PermissionGrantTwinRoleEntity, UUID> entityGetIdFunction() {
        return PermissionGrantTwinRoleEntity::getId;
    }

    @Override
    public boolean isEntityReadDenied(PermissionGrantTwinRoleEntity entity, EntitySmartService.ReadPermissionCheckMode readPermissionCheckMode) throws ServiceException {
        DomainEntity domain = authService.getApiUser().getDomain();
        boolean readDenied = !entity.getPermissionSchema().getDomainId().equals(domain.getId()) &&
                !entity.getTwinClass().getDomainId().equals(domain.getId());
        if (readDenied) {
            EntitySmartService.entityReadDenied(readPermissionCheckMode, domain.easyLog(EasyLoggable.Level.NORMAL) + " is not allowed in domain[" + domain.easyLog(EasyLoggable.Level.NORMAL));
        }
        return readDenied;
    }

    @Override
    public boolean validateEntity(PermissionGrantTwinRoleEntity entity, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
        if (entity.getPermissionSchemaId() == null)
            return logErrorAndReturnFalse(entity.easyLog(EasyLoggable.Level.NORMAL) + " empty permissionSchemaId");
        if (entity.getPermissionId() == null)
            return logErrorAndReturnFalse(entity.easyLog(EasyLoggable.Level.NORMAL) + " empty permissionId");
        if (entity.getTwinClassId() == null)
            return logErrorAndReturnFalse(entity.easyLog(EasyLoggable.Level.NORMAL) + " empty twinClassId");
        if (entity.getGrantedByUserId() == null)
            return logErrorAndReturnFalse(entity.easyLog(EasyLoggable.Level.NORMAL) + " empty grantedByUserId");
        if (entity.getTwinRole() == null)
            return logErrorAndReturnFalse(entity.easyLog(EasyLoggable.Level.NORMAL) + " empty twinRole");

        switch (entityValidateMode) {
            case beforeSave:
                if (entity.getPermissionSchema() == null || !entity.getPermissionSchema().getId().equals(entity.getPermissionSchemaId()))
                    entity.setPermissionSchema(permissionSchemaService.findEntitySafe(entity.getPermissionSchemaId()));
                if (entity.getPermission() == null || !entity.getPermission().getId().equals(entity.getPermissionId()))
                    entity.setPermission(permissionService.findEntitySafe(entity.getPermissionId()));
                if (entity.getTwinClass() == null || !entity.getTwinClass().getId().equals(entity.getTwinClassId()))
                    entity.setTwinClass(twinClassService.findEntitySafe(entity.getTwinClassId()));
                if (entity.getGrantedByUser() == null || !entity.getGrantedByUser().getId().equals(entity.getGrantedByUserId()))
                    entity.setGrantedByUser(userService.findEntitySafe(entity.getGrantedByUserId()));
        }
        return true;
    }

    public PermissionGrantTwinRoleEntity createPermissionGrantTwinRole(PermissionGrantTwinRoleEntity entity) throws ServiceException {
        entity
                .setGrantedByUserId(authService.getApiUser().getUserId())
                .setGrantedAt(Timestamp.from(Instant.now()));
        return saveSafe(entity);
    }

    @Transactional(rollbackFor = Throwable.class)
    public PermissionGrantTwinRoleEntity updatePermissionGrantTwinRole(PermissionGrantTwinRoleEntity entity) throws ServiceException {
        PermissionGrantTwinRoleEntity dbEntity = findEntitySafe(entity.getId());
        ChangesHelper changesHelper = new ChangesHelper();

        updateEntityFieldByEntity(entity, dbEntity, PermissionGrantTwinRoleEntity::getPermissionSchemaId,
                PermissionGrantTwinRoleEntity::setPermissionSchemaId, PermissionGrantTwinRoleEntity.Fields.permissionSchemaId, changesHelper);
        updateEntityFieldByEntity(entity, dbEntity, PermissionGrantTwinRoleEntity::getPermissionId,
                PermissionGrantTwinRoleEntity::setPermissionId, PermissionGrantTwinRoleEntity.Fields.permissionId, changesHelper);
        updateEntityFieldByEntity(entity, dbEntity, PermissionGrantTwinRoleEntity::getTwinClassId,
                PermissionGrantTwinRoleEntity::setTwinClassId, PermissionGrantTwinRoleEntity.Fields.twinClassId, changesHelper);
        updateEntityFieldByEntity(entity, dbEntity, PermissionGrantTwinRoleEntity::getTwinRole,
                PermissionGrantTwinRoleEntity::setTwinRole, PermissionGrantTwinRoleEntity.Fields.twinRole, changesHelper);

        return updateSafe(dbEntity, changesHelper);
    }

    @Transactional
    public void deleteById(UUID id) throws ServiceException {
        deleteSafe(id);
    }
}
