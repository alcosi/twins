package org.twins.core.service.domain;

import io.github.breninsul.logging.aspect.JavaLoggingLevel;
import io.github.breninsul.logging.aspect.annotation.LogExecutionTime;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.twins.core.dao.EntryCount;
import org.twins.core.dao.domain.DomainBusinessAccountUserEntity;
import org.twins.core.dao.domain.DomainBusinessAccountUserRepository;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.businessaccount.BusinessAccountService;
import org.twins.core.service.user.UserService;
import org.twins.core.service.usergroup.UserGroupService;

import java.util.*;

@Slf4j
@Service
@LogExecutionTime(logPrefix = "LONG EXECUTION TIME:", logIfTookMoreThenMs = 2 * 1000, level = JavaLoggingLevel.WARNING)
@Lazy
@AllArgsConstructor
public class DomainBusinessAccountUserService {
    private final UserService userService;
    private final BusinessAccountService businessAccountService;
    private final UserGroupService userGroupService;
    private final DomainBusinessAccountUserRepository domainBusinessAccountUserRepository;
    private final AuthService authService;


    public void loadUser(DomainBusinessAccountUserEntity src) throws ServiceException {
        loadUser(Collections.singletonList(src));
    }

    public void loadUser(Collection<DomainBusinessAccountUserEntity> srcCollection) throws ServiceException {
        userService.load(srcCollection,
                DomainBusinessAccountUserEntity::getUserId,
                DomainBusinessAccountUserEntity::getUser,
                DomainBusinessAccountUserEntity::setUser);
    }

    public void loadBusinessAccount(DomainBusinessAccountUserEntity src) throws ServiceException {
        loadBusinessAccount(Collections.singletonList(src));
    }

    public void loadBusinessAccount(Collection<DomainBusinessAccountUserEntity> srcCollection) throws ServiceException {
        businessAccountService.load(srcCollection,
                DomainBusinessAccountUserEntity::getBusinessAccountId,
                DomainBusinessAccountUserEntity::getBusinessAccount,
                DomainBusinessAccountUserEntity::setBusinessAccount);
    }

    public void loadGroups(DomainBusinessAccountUserEntity src) throws ServiceException {
        loadGroups(Collections.singletonList(src));
    }

    public void loadGroups(Collection<DomainBusinessAccountUserEntity> srcCollection) throws ServiceException {
        userGroupService.loadGroupsForDomainBusinessAccountUsers(srcCollection);
    }

    public List<EntryCount> getUsersCount(Set<UUID> businessAccounts) throws ServiceException {
        return domainBusinessAccountUserRepository.countUsersInBusinessAccounts(businessAccounts, authService.getApiUser().getDomainId());
    }
}
