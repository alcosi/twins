package org.twins.core.service.auth;

import io.github.breninsul.logging.aspect.JavaLoggingLevel;
import io.github.breninsul.logging.aspect.annotation.LogExecutionTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.twins.core.domain.ApiUser;
import org.twins.core.domain.apiuser.*;

import java.util.UUID;


@Slf4j
@Service
@LogExecutionTime(logPrefix = "LONG EXECUTION TIME:", logIfTookMoreThenMs = 2 * 1000, level = JavaLoggingLevel.WARNING)
@Lazy
@RequiredArgsConstructor
public class AuthService {
    @Lazy
    private final ApiUserResolverService apiUserResolverService;

    final ApiUser apiUser;

    private static final ThreadLocal<ApiUser> threadLocalApiUser = new ThreadLocal<>();

    public ApiUser getApiUser() throws ServiceException {
        if (threadLocalApiUser.get() == null)
            return apiUser;
        else // in so,e cases we need to emulate user work, for example it some job is running from scheduler
            return threadLocalApiUser.get();
    }

    public void setThreadLocalApiUser(UUID domainId, UUID businessAccountId, UUID userId) {
        //todo think over ApiUser interface
        ApiUser apiUser = new ApiUser(apiUserResolverService);
        apiUser
                .setMachineUserResolver(MachineUserResolverNotSpecified.instance)
                .setMachineBusinessAccountResolver(MachineBusinessAccountResolverNotSpecified.instance)
                .setUserResolver(new UserResolverGivenId(userId))
                .setDomainResolver(new DomainResolverGivenId(domainId))
                .setBusinessAccountResolver(new BusinessAccountResolverGivenId(businessAccountId));
        threadLocalApiUser.set(apiUser);
    }

    public void removeThreadLocalApiUser() {
        threadLocalApiUser.remove();
    }
}
