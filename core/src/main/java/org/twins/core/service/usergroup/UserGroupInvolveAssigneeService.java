package org.twins.core.service.usergroup;

import io.github.breninsul.logging.aspect.JavaLoggingLevel;
import io.github.breninsul.logging.aspect.annotation.LogExecutionTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.EasyLoggable;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.kit.Kit;
import org.cambium.common.util.ChangesHelper;
import org.cambium.common.util.ChangesHelperMulti;
import org.cambium.common.util.CollectionUtils;

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
import org.twins.core.service.user.UserService;
import org.twins.core.domain.usergroup.UserGroupInvolveAssigneeCreate;
import org.twins.core.domain.usergroup.UserGroupInvolveAssigneeUpdate;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.StreamSupport;

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
        return isDomainAccessDenied(entity.getTwinClass().getDomainId());
    }

    @Override
    public boolean validateEntity(UserGroupInvolveAssigneeEntity entity, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
        if (entity.getUserGroupId() == null)
            return logErrorAndReturnFalse(entity.easyLog(EasyLoggable.Level.NORMAL) + " empty userGroupsId");
        if (entity.getPropagationByTwinClassId() == null)
            return logErrorAndReturnFalse(entity.easyLog(EasyLoggable.Level.NORMAL) + " empty propagationByTwinClassId");
        if (entity.getCreatedByUserId() == null)
            return logErrorAndReturnFalse(entity.easyLog(EasyLoggable.Level.NORMAL) + " empty grantedByUserId");

        switch (entityValidateMode) {
            case beforeSave:
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

    public void loadUserGroup(Collection<UserGroupInvolveAssigneeEntity> entities) throws ServiceException {
        userGroupService.load(entities,
                UserGroupInvolveAssigneeEntity::getId,
                UserGroupInvolveAssigneeEntity::getUserGroupId,
                UserGroupInvolveAssigneeEntity::getUserGroup,
                UserGroupInvolveAssigneeEntity::setUserGroup);
    }

    public void loadTwinClass(Collection<UserGroupInvolveAssigneeEntity> entities) throws ServiceException {
        twinClassService.load(entities,
                UserGroupInvolveAssigneeEntity::getId,
                UserGroupInvolveAssigneeEntity::getPropagationByTwinClassId,
                UserGroupInvolveAssigneeEntity::getTwinClass,
                UserGroupInvolveAssigneeEntity::setTwinClass);
    }

    public void loadTwinStatus(Collection<UserGroupInvolveAssigneeEntity> entities) throws ServiceException {
        twinStatusService.load(entities,
                UserGroupInvolveAssigneeEntity::getId,
                UserGroupInvolveAssigneeEntity::getPropagationByTwinStatusId,
                UserGroupInvolveAssigneeEntity::getTwinStatus,
                UserGroupInvolveAssigneeEntity::setTwinStatus);
    }

    public void loadCreatedByUser(Collection<UserGroupInvolveAssigneeEntity> entities) throws ServiceException {
        userService.load(entities,
                UserGroupInvolveAssigneeEntity::getId,
                UserGroupInvolveAssigneeEntity::getCreatedByUserId,
                UserGroupInvolveAssigneeEntity::getCreatedByUser,
                UserGroupInvolveAssigneeEntity::setCreatedByUser);
    }

    @Override
    public boolean validateEntities(Collection<UserGroupInvolveAssigneeEntity> entities, EntitySmartService.EntityValidateMode entityValidateMode) {
        switch (entityValidateMode) {
            case beforeSave -> {
                try {
                    loadUserGroup(entities);
                } catch (ServiceException e) {
                    return logErrorAndReturnFalse("Failed to load user groups");
                }
                try {
                    loadTwinClass(entities);
                } catch (ServiceException e) {
                    return logErrorAndReturnFalse("Failed to load twin classes");
                }
                try {
                    loadTwinStatus(entities);
                } catch (ServiceException e) {
                    return logErrorAndReturnFalse("Failed to load twin statuses");
                }
                try {
                    loadCreatedByUser(entities);
                } catch (ServiceException e) {
                    return logErrorAndReturnFalse("Failed to load created by users");
                }
            }
        }
        return true;
    }


    @Transactional(rollbackFor = Throwable.class)
    public List<UserGroupInvolveAssigneeEntity> createUserGroupInvolveAssignee(Collection<UserGroupInvolveAssigneeCreate> entities) throws ServiceException {
        if (CollectionUtils.isEmpty(entities)) {
            return Collections.emptyList();
        }

        UUID currentUserId = authService.getApiUser().getUserId();
        Timestamp currentTime = Timestamp.from(Instant.now());

        List<UserGroupInvolveAssigneeEntity> entitiesToSave = new ArrayList<>(entities.size());
        for (UserGroupInvolveAssigneeCreate userGroupInvolveAssignee : entities) {
            UserGroupInvolveAssigneeEntity entity = new UserGroupInvolveAssigneeEntity()
                    .setUserGroupId(userGroupInvolveAssignee.getUserGroupId())
                    .setPropagationByTwinClassId(userGroupInvolveAssignee.getPropagationByTwinClassId())
                    .setPropagationByTwinStatusId(userGroupInvolveAssignee.getPropagationByTwinStatusId())
                    .setCreatedByUserId(currentUserId)
                    .setCreatedAt(currentTime);
            entitiesToSave.add(entity);
        }

        return StreamSupport.stream(saveSafe(entitiesToSave).spliterator(), false).toList();
    }

    @Transactional(rollbackFor = Throwable.class)
    public List<UserGroupInvolveAssigneeEntity> updateUserGroupInvolveAssignee(Collection<UserGroupInvolveAssigneeUpdate> entities) throws ServiceException {
        if (CollectionUtils.isEmpty(entities)) {
            return Collections.emptyList();
        }

        Kit<UserGroupInvolveAssigneeEntity, UUID> entitiesKit = findEntitiesSafe(
                entities.stream()
                        .map(UserGroupInvolveAssigneeUpdate::getId)
                        .toList()
        );

        ChangesHelperMulti<UserGroupInvolveAssigneeEntity> changes = new ChangesHelperMulti<>();

        for (UserGroupInvolveAssigneeUpdate userGroupInvolveAssignee : entities) {
            UserGroupInvolveAssigneeEntity dbEntity = entitiesKit.get(userGroupInvolveAssignee.getId());

            ChangesHelper changesHelper = new ChangesHelper();
            updateEntityFieldByValue(userGroupInvolveAssignee.getUserGroupId(), dbEntity,
                    UserGroupInvolveAssigneeEntity::getUserGroupId, UserGroupInvolveAssigneeEntity::setUserGroupId,
                    UserGroupInvolveAssigneeEntity.Fields.userGroupId, changesHelper);
            updateEntityFieldByValue(userGroupInvolveAssignee.getPropagationByTwinClassId(), dbEntity,
                    UserGroupInvolveAssigneeEntity::getPropagationByTwinClassId, UserGroupInvolveAssigneeEntity::setPropagationByTwinClassId,
                    UserGroupInvolveAssigneeEntity.Fields.propagationByTwinClassId, changesHelper);
            updateEntityFieldByValue(userGroupInvolveAssignee.getPropagationByTwinStatusId(), dbEntity,
                    UserGroupInvolveAssigneeEntity::getPropagationByTwinStatusId, UserGroupInvolveAssigneeEntity::setPropagationByTwinStatusId,
                    UserGroupInvolveAssigneeEntity.Fields.propagationByTwinStatusId, changesHelper);

            changes.add(dbEntity, changesHelper);
        }

        updateSafe(changes);
        return entitiesKit.getList();
    }

    @Transactional
    public void deleteById(UUID id) throws ServiceException {
        deleteSafe(id);
    }
}
