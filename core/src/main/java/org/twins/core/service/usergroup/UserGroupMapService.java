package org.twins.core.service.usergroup;

import io.github.breninsul.logging.aspect.JavaLoggingLevel;
import io.github.breninsul.logging.aspect.annotation.LogExecutionTime;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.service.EntitySmartService;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.twins.core.dao.usergroup.UserGroupMapEntity;
import org.twins.core.dao.usergroup.UserGroupMapRepository;
import org.twins.core.service.TwinsEntitySecureFindService;
import org.twins.core.service.businessaccount.BusinessAccountService;
import org.twins.core.service.domain.DomainService;
import org.twins.core.service.user.UserService;

import java.util.Collection;
import java.util.Collections;
import java.util.UUID;
import java.util.function.Function;

@Slf4j
@Service
@LogExecutionTime(logPrefix = "LONG EXECUTION TIME:", logIfTookMoreThenMs = 2 * 1000, level = JavaLoggingLevel.WARNING)
@RequiredArgsConstructor
public class UserGroupMapService extends TwinsEntitySecureFindService<UserGroupMapEntity> {
    @Getter
    private final UserGroupMapRepository repository;
    @Lazy
    private final UserGroupService userGroupService;
    @Lazy
    private final DomainService domainService;
    @Lazy
    private final UserService userService;
    @Lazy
    private final BusinessAccountService businessAccountService;

    @Override
    public CrudRepository<UserGroupMapEntity, UUID> entityRepository() {
        return repository;
    }

    @Override
    public Function<UserGroupMapEntity, UUID> entityGetIdFunction() {
        return UserGroupMapEntity::getId;
    }

    @Override
    public boolean isEntityReadDenied(UserGroupMapEntity entity, EntitySmartService.ReadPermissionCheckMode readPermissionCheckMode) throws ServiceException {
        return false;
    }

    @Override
    public boolean validateEntity(UserGroupMapEntity entity, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
        return true;
    }

    public void loadUserGroup(UserGroupMapEntity src) throws ServiceException {
        loadUserGroup(Collections.singletonList(src));
    }

    public void loadUserGroup(Collection<UserGroupMapEntity> srcCollection) throws ServiceException {
        userGroupService.load(srcCollection,
                UserGroupMapEntity::getUserGroupId,
                UserGroupMapEntity::getUserGroup,
                UserGroupMapEntity::setUserGroup);
    }

    public void loadDomain(UserGroupMapEntity src) throws ServiceException {
        loadDomain(Collections.singletonList(src));
    }

    public void loadDomain(Collection<UserGroupMapEntity> srcCollection) throws ServiceException {
        domainService.load(srcCollection,
                UserGroupMapEntity::getDomainId,
                UserGroupMapEntity::getDomain,
                UserGroupMapEntity::setDomain);
    }

    public void loadUser(UserGroupMapEntity src) throws ServiceException {
        loadUser(Collections.singletonList(src));
    }

    public void loadUser(Collection<UserGroupMapEntity> srcCollection) throws ServiceException {
        userService.load(srcCollection,
                UserGroupMapEntity::getUserId,
                UserGroupMapEntity::getUser,
                UserGroupMapEntity::setUser);
    }

    public void loadAddedByUser(UserGroupMapEntity src) throws ServiceException {
        loadAddedByUser(Collections.singletonList(src));
    }

    public void loadAddedByUser(Collection<UserGroupMapEntity> srcCollection) throws ServiceException {
        userService.load(srcCollection,
                UserGroupMapEntity::getAddedByUserId,
                UserGroupMapEntity::getAddedByUser,
                UserGroupMapEntity::setAddedByUser);
    }

    public void loadBusinessAccount(UserGroupMapEntity src) throws ServiceException {
        loadBusinessAccount(Collections.singletonList(src));
    }

    public void loadBusinessAccount(Collection<UserGroupMapEntity> srcCollection) throws ServiceException {
        businessAccountService.load(srcCollection,
                UserGroupMapEntity::getBusinessAccountId,
                UserGroupMapEntity::getBusinessAccount,
                UserGroupMapEntity::setBusinessAccount);
    }
}
