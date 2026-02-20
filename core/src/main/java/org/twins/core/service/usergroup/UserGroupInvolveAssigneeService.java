package org.twins.core.service.usergroup;

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
import org.twins.core.dao.usergroup.UserGroupInvolveAssigneeEntity;
import org.twins.core.dao.usergroup.UserGroupInvolveAssigneeRepository;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.permission.PermissionSchemaService;
import org.twins.core.service.twin.TwinStatusService;
import org.twins.core.service.twinclass.TwinClassService;
import org.twins.core.service.user.UserGroupService;
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
public class UserGroupInvolveAssigneeService extends EntitySecureFindServiceImpl<UserGroupInvolveAssigneeEntity> {
    @Getter
    private final UserGroupInvolveAssigneeRepository repository;
    private final PermissionSchemaService permissionSchemaService;
    private final UserGroupService userGroupService;
    private final AuthService authService;
    private final TwinClassService twinClassService;
    private final TwinStatusService twinStatusService;
    private final UserService userService;

    @Override
    public CrudRepository<UserGroupInvolveAssigneeEntity, UUID> entityRepository() {
        return repository;
    }

    @Override
    public Function<UserGroupInvolveAssigneeEntity, UUID> entityGetIdFunction() {
        return UserGroupInvolveAssigneeEntity::getId;
    }

    @Override
    public boolean isEntityReadDenied(UserGroupInvolveAssigneeEntity entity, EntitySmartService.ReadPermissionCheckMode readPermissionCheckMode) throws ServiceException {
        DomainEntity domain = authService.getApiUser().getDomain();
        boolean readDenied = !entity.getTwinClass().getDomainId().equals(domain.getId());
        if (readDenied) {
            EntitySmartService.entityReadDenied(readPermissionCheckMode, domain.easyLog(EasyLoggable.Level.NORMAL) + " is not allowed in domain[" + domain.easyLog(EasyLoggable.Level.NORMAL));
        }
        return readDenied;
    }

    @Override
    public boolean validateEntity(UserGroupInvolveAssigneeEntity entity, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
        if (entity.getPermissionSchemaId() == null)
            return logErrorAndReturnFalse(entity.easyLog(EasyLoggable.Level.NORMAL) + " empty permissionSchemaId");
        if (entity.getUserGroupId() == null)
            return logErrorAndReturnFalse(entity.easyLog(EasyLoggable.Level.NORMAL) + " empty userGroupsId");
        if (entity.getPropagationByTwinClassId() == null)
            return logErrorAndReturnFalse(entity.easyLog(EasyLoggable.Level.NORMAL) + " empty propagationByTwinClassId");
        if (entity.getCreatedByUserId() == null)
            return logErrorAndReturnFalse(entity.easyLog(EasyLoggable.Level.NORMAL) + " empty grantedByUserId");

        switch (entityValidateMode) {
            case beforeSave:
                if (entity.getPermissionSchema() == null || !entity.getPermissionSchema().getId().equals(entity.getPermissionSchemaId()))
                    entity.setPermissionSchema(permissionSchemaService.findEntitySafe(entity.getPermissionSchemaId()));
                if (entity.getUserGroup() == null || !entity.getUserGroup().getId().equals(entity.getUserGroupId()))
                    entity.setUserGroup(userGroupService.findEntitySafe(entity.getUserGroupId()));
                if (entity.getTwinClass() == null || !entity.getTwinClass().getId().equals(entity.getPropagationByTwinClassId()))
                    entity.setTwinClass(twinClassService.findEntitySafe(entity.getPropagationByTwinClassId()));
                if (entity.getCreatedByUser() == null || !entity.getCreatedByUser().getId().equals(entity.getCreatedByUserId()))
                    entity.setCreatedByUser(userService.findEntitySafe(entity.getCreatedByUserId()));
                if (entity.getPropagationByTwinStatusId() != null && (entity.getTwinStatus() == null || !entity.getTwinStatus().getId().equals(entity.getPropagationByTwinStatusId())))
                    entity.setTwinStatus(twinStatusService.findEntitySafe(entity.getPropagationByTwinStatusId()));
        }
        return true;
    }

    public UserGroupInvolveAssigneeEntity createUserGroupByAssigneePropagationEntity(UserGroupInvolveAssigneeEntity entity) throws ServiceException {
        entity
                .setCreatedByUserId(authService.getApiUser().getUserId())
                .setCreatedAt(Timestamp.from(Instant.now()));
        return saveSafe(entity);
    }

    @Transactional(rollbackFor = Throwable.class)
    public UserGroupInvolveAssigneeEntity updateUserGroupByAssigneePropagationEntity(UserGroupInvolveAssigneeEntity entity) throws ServiceException {
        UserGroupInvolveAssigneeEntity dbEntity = findEntitySafe(entity.getId());
        ChangesHelper changesHelper = new ChangesHelper();

        updateEntityFieldByEntity(entity, dbEntity, UserGroupInvolveAssigneeEntity::getPermissionSchemaId,
                UserGroupInvolveAssigneeEntity::setPermissionSchemaId, UserGroupInvolveAssigneeEntity.Fields.permissionSchemaId ,changesHelper);
        updateEntityFieldByEntity(entity, dbEntity, UserGroupInvolveAssigneeEntity::getUserGroupId,
                UserGroupInvolveAssigneeEntity::setUserGroupId, UserGroupInvolveAssigneeEntity.Fields.userGroupId ,changesHelper);
        updateEntityFieldByEntity(entity, dbEntity, UserGroupInvolveAssigneeEntity::getPropagationByTwinClassId,
                UserGroupInvolveAssigneeEntity::setPropagationByTwinClassId, UserGroupInvolveAssigneeEntity.Fields.propagationByTwinClassId ,changesHelper);
        updateEntityFieldByEntity(entity, dbEntity, UserGroupInvolveAssigneeEntity::getPropagationByTwinStatusId,
                UserGroupInvolveAssigneeEntity::setPropagationByTwinStatusId, UserGroupInvolveAssigneeEntity.Fields.propagationByTwinStatusId ,changesHelper);

        return updateSafe(dbEntity, changesHelper);
    }

    @Transactional
    public void deleteById(UUID id) throws ServiceException {
        deleteSafe(id);
    }
}
