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
import org.twins.core.dao.permission.PermissionGrantAssigneePropagationEntity;
import org.twins.core.dao.permission.PermissionGrantAssigneePropagationRepository;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.twin.TwinStatusService;
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
public class PermissionGrantAssigneePropagationService extends EntitySecureFindServiceImpl<PermissionGrantAssigneePropagationEntity> {
    @Getter
    private final PermissionGrantAssigneePropagationRepository repository;
    private final PermissionSchemaService permissionSchemaService;
    private final PermissionService permissionService;
    private final AuthService authService;
    private final TwinClassService twinClassService;
    private final TwinStatusService twinStatusService;
    private final UserService userService;

    @Override
    public CrudRepository<PermissionGrantAssigneePropagationEntity, UUID> entityRepository() {
        return repository;
    }

    @Override
    public Function<PermissionGrantAssigneePropagationEntity, UUID> entityGetIdFunction() {
        return PermissionGrantAssigneePropagationEntity::getId;
    }

    @Override
    public boolean isEntityReadDenied(PermissionGrantAssigneePropagationEntity entity, EntitySmartService.ReadPermissionCheckMode readPermissionCheckMode) throws ServiceException {
        DomainEntity domain = authService.getApiUser().getDomain();
        boolean readDenied = !entity.getTwinClass().getDomainId().equals(domain.getId());
        if (readDenied) {
            EntitySmartService.entityReadDenied(readPermissionCheckMode, domain.easyLog(EasyLoggable.Level.NORMAL) + " is not allowed in domain[" + domain.easyLog(EasyLoggable.Level.NORMAL));
        }
        return readDenied;
    }

    @Override
    public boolean validateEntity(PermissionGrantAssigneePropagationEntity entity, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
        if (entity.getPermissionSchemaId() == null)
            return logErrorAndReturnFalse(entity.easyLog(EasyLoggable.Level.NORMAL) + " empty permissionSchemaId");
        if (entity.getPermissionId() == null)
            return logErrorAndReturnFalse(entity.easyLog(EasyLoggable.Level.NORMAL) + " empty permissionId");
        if (entity.getPropagationByTwinClassId() == null)
            return logErrorAndReturnFalse(entity.easyLog(EasyLoggable.Level.NORMAL) + " empty propagationByTwinClassId");
        if (entity.getGrantedByUserId() == null)
            return logErrorAndReturnFalse(entity.easyLog(EasyLoggable.Level.NORMAL) + " empty grantedByUserId");

        switch (entityValidateMode) {
            case beforeSave:
                if (entity.getPermissionSchema() == null || !entity.getPermissionSchema().getId().equals(entity.getPermissionSchemaId()))
                    entity.setPermissionSchema(permissionSchemaService.findEntitySafe(entity.getPermissionSchemaId()));
                if (entity.getPermission() == null || !entity.getPermission().getId().equals(entity.getPermissionId()))
                    entity.setPermission(permissionService.findEntitySafe(entity.getPermissionId()));
                if (entity.getTwinClass() == null || !entity.getTwinClass().getId().equals(entity.getPropagationByTwinClassId()))
                    entity.setTwinClass(twinClassService.findEntitySafe(entity.getPropagationByTwinClassId()));
                if (entity.getGrantedByUser() == null || !entity.getGrantedByUser().getId().equals(entity.getGrantedByUserId()))
                    entity.setGrantedByUser(userService.findEntitySafe(entity.getGrantedByUserId()));
                if (entity.getPropagationByTwinStatusId() != null && (entity.getTwinStatus() == null || !entity.getTwinStatus().getId().equals(entity.getPropagationByTwinStatusId())))
                    entity.setTwinStatus(twinStatusService.findEntitySafe(entity.getPropagationByTwinStatusId()));
        }
        return true;
    }

    public PermissionGrantAssigneePropagationEntity createPermissionGrantAssigneePropagationEntity(PermissionGrantAssigneePropagationEntity entity) throws ServiceException {
        entity
                .setGrantedByUserId(authService.getApiUser().getUserId())
                .setGrantedAt(Timestamp.from(Instant.now()));
        return saveSafe(entity);
    }

    @Transactional(rollbackFor = Throwable.class)
    public PermissionGrantAssigneePropagationEntity updatePermissionGrantAssigneePropagationEntity(PermissionGrantAssigneePropagationEntity entity) throws ServiceException {
        PermissionGrantAssigneePropagationEntity dbEntity = findEntitySafe(entity.getId());
        ChangesHelper changesHelper = new ChangesHelper();

        updateEntityFieldByEntity(entity, dbEntity, PermissionGrantAssigneePropagationEntity::getPermissionSchemaId,
                PermissionGrantAssigneePropagationEntity::setPermissionSchemaId, PermissionGrantAssigneePropagationEntity.Fields.permissionSchemaId ,changesHelper);
        updateEntityFieldByEntity(entity, dbEntity, PermissionGrantAssigneePropagationEntity::getPermissionId,
                PermissionGrantAssigneePropagationEntity::setPermissionId, PermissionGrantAssigneePropagationEntity.Fields.permissionId ,changesHelper);
        updateEntityFieldByEntity(entity, dbEntity, PermissionGrantAssigneePropagationEntity::getPropagationByTwinClassId,
                PermissionGrantAssigneePropagationEntity::setPropagationByTwinClassId, PermissionGrantAssigneePropagationEntity.Fields.propagationByTwinClassId ,changesHelper);
        updateEntityFieldByEntity(entity, dbEntity, PermissionGrantAssigneePropagationEntity::getPropagationByTwinStatusId,
                PermissionGrantAssigneePropagationEntity::setPropagationByTwinStatusId, PermissionGrantAssigneePropagationEntity.Fields.propagationByTwinStatusId ,changesHelper);
        updateEntityFieldByEntity(entity, dbEntity, PermissionGrantAssigneePropagationEntity::getInSpaceOnly,
                PermissionGrantAssigneePropagationEntity::setInSpaceOnly, PermissionGrantAssigneePropagationEntity.Fields.inSpaceOnly ,changesHelper);

        return updateSafe(dbEntity, changesHelper);
    }

    @Transactional
    public void deleteById(UUID id) throws ServiceException {
        deleteSafe(id);
    }
}