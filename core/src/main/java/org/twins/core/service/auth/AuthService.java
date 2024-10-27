package org.twins.core.service.auth;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.service.EntitySmartService;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.twins.core.dao.businessaccount.BusinessAccountRepository;
import org.twins.core.dao.businessaccount.BusinessAccountUserRepository;
import org.twins.core.dao.domain.DomainBusinessAccountRepository;
import org.twins.core.dao.domain.DomainRepository;
import org.twins.core.dao.domain.DomainUserRepository;
import org.twins.core.dao.user.UserRepository;
import org.twins.core.domain.ApiUser;
import org.twins.core.domain.apiuser.*;

import java.util.UUID;


@Slf4j
@Service
@Lazy
@RequiredArgsConstructor
public class AuthService {
    private final ApplicationContext applicationContext;
//    final HttpRequestService httpRequestService;
//    final UserRepository userRepository;
//    final BusinessAccountRepository businessAccountRepository;
//    final DomainRepository domainRepository;
//    @Lazy
//    final DomainService domainService;
//    final EntitySmartService entitySmartService;
//    @Lazy
//    final FeaturerService featurerService;
//    private ApiUser apiUser = null;
//
//    public ApiUser getApiUser() throws ServiceException {
//        if (apiUser != null)
//            return apiUser;
//        UUID domainId;
//        try {
//            domainId = UUID.fromString(httpRequestService.getDomainIdFromRequest());
//        } catch (Exception e) {
//            throw new ServiceException(ErrorCodeTwins.UUID_UNKNOWN, "Incorrect domain uuid");
//        }
//        String authToken = httpRequestService.getAuthTokenFromRequest();
//        if (StringUtils.isEmpty(authToken)) //todo delete on production
//            authToken = httpRequestService.getBusinessAccountIdFromRequest() + "," + httpRequestService.getUserIdFromRequest();
//        DomainEntity domainEntity = domainService.findDomain(domainId, EntitySmartService.FindMode.ifEmptyThrows);
//        String channel = httpRequestService.getChannelIdFromRequest();
//        TokenHandler tokenHandler = featurerService.getFeaturer(domainEntity.getTokenHandlerFeaturer(), TokenHandler.class);
//        apiUser = tokenHandler.resolveUserIdAndBusinessAccountId(domainEntity.getTokenHandlerParams(), authToken, domainEntity, Channel.resolve(channel));
//        return apiUser;
//        //todo store ApiUser in session context
//    }

    final ApiUser apiUser;

    private static final ThreadLocal<ApiUser> threadLocalApiUser = new ThreadLocal<>();

    public ApiUser getApiUser() throws ServiceException {
        if (RequestContextHolder.getRequestAttributes() != null)
            return apiUser;
        else
            return threadLocalApiUser.get();
    }

    public void setThreadLocalApiUser(UUID domainId, UUID businessAccountId, UUID userId) {
        //todo think over ApiUser interface
        ApiUser apiUser = new ApiUser(
                applicationContext.getBean(EntitySmartService.class),
                applicationContext.getBean(DomainRepository.class),
                applicationContext.getBean(DomainUserRepository.class),
                applicationContext.getBean(DomainBusinessAccountRepository.class),
                applicationContext.getBean(BusinessAccountRepository.class),
                applicationContext.getBean(BusinessAccountUserRepository.class),
                applicationContext.getBean(UserRepository.class),
                applicationContext.getBean(DomainResolverHeaders.class),
                applicationContext.getBean(LocaleResolverDomainUser.class),
                applicationContext.getBean(LocaleResolverHeader.class),
                applicationContext.getBean(UserBusinessAccountResolverAuthToken.class));
        apiUser
                .setUserResolver(new UserResolverGivenId(userId))
                .setDomainResolver(new DomainResolverGivenId(domainId))
                .setBusinessAccountResolver(new BusinessAccountResolverGivenId(businessAccountId));

        threadLocalApiUser.set(apiUser);
    }

    public void setThreadLocalApiUser(ApiUser apiUser) {
        threadLocalApiUser.set(apiUser);
    }

    public void removeThreadLocalApiUser() {
        threadLocalApiUser.remove();
    }
}
