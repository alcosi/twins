package org.twins.core.service.domain;

import io.github.breninsul.logging.aspect.JavaLoggingLevel;
import io.github.breninsul.logging.aspect.annotation.LogExecutionTime;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.twins.core.dao.domain.DomainBusinessAccountUserEntity;
import org.twins.core.service.businessaccount.BusinessAccountService;
import org.twins.core.service.user.UserService;

import java.util.Collection;
import java.util.Collections;

@Slf4j
@Service
@LogExecutionTime(logPrefix = "LONG EXECUTION TIME:", logIfTookMoreThenMs = 2 * 1000, level = JavaLoggingLevel.WARNING)
@Lazy
@AllArgsConstructor
public class DomainBusinessAccountUserService {
    private final UserService userService;
    private final BusinessAccountService businessAccountService;


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
}
