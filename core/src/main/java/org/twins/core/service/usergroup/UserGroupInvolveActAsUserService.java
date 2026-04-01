package org.twins.core.service.usergroup;

import io.github.breninsul.logging.aspect.JavaLoggingLevel;
import io.github.breninsul.logging.aspect.annotation.LogExecutionTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import org.twins.core.dao.user.UserGroupEntity;
import org.twins.core.dao.usergroup.UserGroupInvolveActAsUserEntity;
import org.twins.core.dao.usergroup.UserGroupInvolveActAsUserRepository;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.user.UserService;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.*;
import java.util.function.Function;
import java.util.stream.StreamSupport;

@Slf4j
@Service
@LogExecutionTime(logPrefix = "LONG EXECUTION TIME:", logIfTookMoreThenMs = 2 * 1000, level = JavaLoggingLevel.WARNING)
@Lazy
@RequiredArgsConstructor
public class UserGroupInvolveActAsUserService extends EntitySecureFindServiceImpl<UserGroupInvolveActAsUserEntity> {

    private final UserGroupInvolveActAsUserRepository repository;
    private final AuthService authService;
    private final UserService userService;
    private final UserGroupService userGroupService;

    @Override
    public CrudRepository<UserGroupInvolveActAsUserEntity, UUID> entityRepository() {
        return repository;
    }

    @Override
    public Function<UserGroupInvolveActAsUserEntity, UUID> entityGetIdFunction() {
        return UserGroupInvolveActAsUserEntity::getId;
    }

    @Override
    public boolean isEntityReadDenied(UserGroupInvolveActAsUserEntity entity, EntitySmartService.ReadPermissionCheckMode readPermissionCheckMode) throws ServiceException {
        //todo denied if user is not registered in current domain
        return false;
    }

    @Override
    public boolean validateEntity(UserGroupInvolveActAsUserEntity entity, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
        return userService.checkUserRegisteredInDomain(entity.getMachineUserId(), entity.getDomainId());
    }

    public List<UserGroupEntity> findByMachineUserIdAndDomainId(UUID machineUserId, UUID domainId) {
        return repository.findByMachineUserIdAndDomainId(machineUserId, domainId);
    }

    @Transactional(rollbackFor = Throwable.class)
    public List<UserGroupInvolveActAsUserEntity> createUserGroupInvolveActAsUser(Collection<UserGroupInvolveActAsUserEntity> entities) throws ServiceException {
        if (CollectionUtils.isEmpty(entities)) {
            return Collections.emptyList();
        }
        UUID domainId = authService.getApiUser().getDomainId();
        UUID userId = authService.getApiUser().getUserId();
        entities.forEach(it -> {
            it.setDomainId(domainId);
            it.setAddedByUserId(userId);
            it.setAddedAt(Timestamp.from(Instant.now()));
        });

        return StreamSupport.stream(saveSafe(entities).spliterator(), false).toList();
    }

    @Transactional(rollbackFor = Throwable.class)
    public List<UserGroupInvolveActAsUserEntity> updateUserGroupInvolveActAsUser(Collection<UserGroupInvolveActAsUserEntity> entities) throws ServiceException {
        if (CollectionUtils.isEmpty(entities)) {
            return Collections.emptyList();
        }

        ChangesHelperMulti<UserGroupInvolveActAsUserEntity> changes = new ChangesHelperMulti<>();
        Kit<UserGroupInvolveActAsUserEntity, UUID> entitiesKit = findEntitiesSafe(entities.stream().map(UserGroupInvolveActAsUserEntity::getId).toList());
        List<UserGroupInvolveActAsUserEntity> allEntities = new ArrayList<>(entities.size());

        for (UserGroupInvolveActAsUserEntity userGroupInvolveActAsUser : entities) {
            UserGroupInvolveActAsUserEntity entity = entitiesKit.get(userGroupInvolveActAsUser.getId());
            allEntities.add(entity);
            ChangesHelper changesHelper = new ChangesHelper();
            updateEntityFieldByValue(userGroupInvolveActAsUser.getMachineUserId(), entity, UserGroupInvolveActAsUserEntity::getMachineUserId, UserGroupInvolveActAsUserEntity::setMachineUserId, UserGroupInvolveActAsUserEntity.Fields.machineUserId, changesHelper);
            updateEntityFieldByValue(userGroupInvolveActAsUser.getUserGroupId(), entity, UserGroupInvolveActAsUserEntity::getUserGroupId, UserGroupInvolveActAsUserEntity::setUserGroupId, UserGroupInvolveActAsUserEntity.Fields.userGroupId, changesHelper);

            changes.add(entity, changesHelper);
        }
        updateSafe(changes);
        return allEntities;
    }

    public void loadMachineUser(UserGroupInvolveActAsUserEntity src) throws ServiceException {
        loadMachineUser(Collections.singletonList(src));
    }

    public void loadMachineUser(Collection<UserGroupInvolveActAsUserEntity> srcCollection) throws ServiceException {
        userService.load(srcCollection,
                UserGroupInvolveActAsUserEntity::getId,
                UserGroupInvolveActAsUserEntity::getMachineUserId,
                UserGroupInvolveActAsUserEntity::getMachineUser,
                UserGroupInvolveActAsUserEntity::setMachineUser);
    }

    public void loadAddedByUser(UserGroupInvolveActAsUserEntity src) throws ServiceException {
        loadAddedByUser(Collections.singletonList(src));
    }

    public void loadAddedByUser(Collection<UserGroupInvolveActAsUserEntity> srcCollection) throws ServiceException {
        userService.load(srcCollection,
                UserGroupInvolveActAsUserEntity::getId,
                UserGroupInvolveActAsUserEntity::getAddedByUserId,
                UserGroupInvolveActAsUserEntity::getAddedByUser,
                UserGroupInvolveActAsUserEntity::setAddedByUser);
    }

    public void loadUserGroup(UserGroupInvolveActAsUserEntity src) throws ServiceException {
        loadUserGroup(Collections.singletonList(src));
    }

    public void loadUserGroup(Collection<UserGroupInvolveActAsUserEntity> srcCollection) throws ServiceException {
        userGroupService.load(srcCollection,
                UserGroupInvolveActAsUserEntity::getId,
                UserGroupInvolveActAsUserEntity::getUserGroupId,
                UserGroupInvolveActAsUserEntity::getUserGroup,
                UserGroupInvolveActAsUserEntity::setUserGroup);
    }
}