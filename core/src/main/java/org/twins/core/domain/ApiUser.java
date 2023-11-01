package org.twins.core.domain;

import lombok.RequiredArgsConstructor;
import org.cambium.common.exception.ServiceException;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;
import org.twins.core.dao.businessaccount.BusinessAccountEntity;
import org.twins.core.dao.businessaccount.BusinessAccountRepository;
import org.twins.core.dao.domain.DomainEntity;
import org.twins.core.dao.domain.DomainRepository;
import org.twins.core.dao.user.UserEntity;
import org.twins.core.dao.user.UserRepository;
import org.twins.core.domain.apiuser.*;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.service.EntitySmartService;

import java.util.UUID;

@Component
@RequestScope
@RequiredArgsConstructor
public class ApiUser {
    private DomainEntity domain;
    private DomainResolver domainResolver;
    private UserEntity user;
    private BusinessAccountEntity businessAccount;
    private BusinessAccountResolver businessAccountResolver;
    private UserResolver userResolver;
    private Channel channel;

    final EntitySmartService entitySmartService;
    final DomainRepository domainRepository;
    final BusinessAccountRepository businessAccountRepository;
    final UserRepository userRepository;
    final DomainResolverHeaders domainResolverHeaders;
    final UserBusinessAccountResolverAuthToken userBusinessAccountResolverAuthToken;

    public ApiUser setDomainResolver(DomainResolver domainResolver) {
        this.domainResolver = domainResolver;
        return this;
    }

    public ApiUser setBusinessAccountResolver(BusinessAccountResolver businessAccountResolver) {
        this.businessAccountResolver = businessAccountResolver;
        return this;
    }

    public ApiUser setUserResolver(UserResolver userResolver) {
        this.userResolver = userResolver;
        return this;
    }

    public DomainEntity getDomain() throws ServiceException {
        if (domain == null) {
            if (domainResolver == null)
                domainResolver = domainResolverHeaders;
            UUID domainId = domainResolver.resolveCurrentDomainId();
            if (domainId == null)
                throw new ServiceException(ErrorCodeTwins.DOMAIN_UNKNOWN);
            domain = entitySmartService.findById(domainId, domainRepository, EntitySmartService.FindMode.ifEmptyThrows);
        }
        return domain;
    }

    public UserEntity getUser() throws ServiceException {
        if (user == null) {
            if (userResolver == null)
                userResolver = userBusinessAccountResolverAuthToken;
            UUID userId = userResolver.resolveCurrentUserId();
            if (userId == null)
                throw new ServiceException(ErrorCodeTwins.USER_UNKNOWN);
            user = entitySmartService.findById(userId, userRepository, EntitySmartService.FindMode.ifEmptyThrows);
        }
        return user;
    }

    public BusinessAccountEntity getBusinessAccount() throws ServiceException {
        if (businessAccount == null) {
            if (businessAccountResolver == null)
                businessAccountResolver = userBusinessAccountResolverAuthToken;
            UUID businessAccountId = businessAccountResolver.resolveCurrentBusinessAccountId();
            if (businessAccountId == null)
                throw new ServiceException(ErrorCodeTwins.BUSINESS_ACCOUNT_UNKNOWN);
            businessAccount = entitySmartService.findById(businessAccountId, businessAccountRepository, EntitySmartService.FindMode.ifEmptyThrows);
        }
        return businessAccount;
    }

    public Channel getChannel() {
        return Channel.WEB; //todo fix
    }
}
