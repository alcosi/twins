package org.twins.core.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.util.CollectionUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;
import org.twins.core.dao.businessaccount.BusinessAccountEntity;
import org.twins.core.dao.businessaccount.BusinessAccountRepository;
import org.twins.core.dao.businessaccount.BusinessAccountUserEntity;
import org.twins.core.dao.businessaccount.BusinessAccountUserRepository;
import org.twins.core.dao.domain.*;
import org.twins.core.dao.user.UserEntity;
import org.twins.core.dao.user.UserRepository;
import org.twins.core.domain.apiuser.*;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.service.EntitySmartService;

import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Component
@RequestScope
@RequiredArgsConstructor
@Slf4j
public class ApiUser {
    private DomainEntity domain;
    private BusinessAccountEntity businessAccount;
    private UserEntity user;
    private UUID domainId;
    private UUID businessAccountId;
    private UUID userId;
    private DomainResolver domainResolver;
    private BusinessAccountResolver businessAccountResolver;
    private UserResolver userResolver;
    private Channel channel;
    private Locale locale;
    private LocaleResolver localeResolver;
    public static final UUID NOT_SPECIFIED = UUID.fromString("ffffffff-ffff-ffff-ffff-ffffffffffff");

    @Getter
    private final UUID requestId = UUID.randomUUID();

    @Getter
    @Setter
    private Set<UUID> permissions;

    @Getter
    @Setter
    private Set<UUID> userGroups;

    @Getter
    @Setter
    private boolean checkMembershipMode = true;

    final EntitySmartService entitySmartService;
    final DomainRepository domainRepository;
    final DomainUserRepository domainUserRepository;
    final DomainBusinessAccountRepository domainBusinessAccountRepository;
    final BusinessAccountRepository businessAccountRepository;
    final BusinessAccountUserRepository businessAccountUserRepository;
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

    public ApiUser setLocaleResolver(LocaleResolver localeResolver) {
        this.localeResolver = localeResolver;
        return this;
    }

    public DomainEntity getDomain() throws ServiceException {
        if (domain != null)
            return domain;
        //we do not call loadDBU because we need domain to detect tokenHandler to resolve userId and businessAccountId
        //otherwise we will get endless loop
        resolveDomainId();
        if (domainId == null)
            throw new ServiceException(ErrorCodeTwins.DOMAIN_UNKNOWN);
        domain = entitySmartService.findById(domainId, domainRepository, EntitySmartService.FindMode.ifEmptyThrows);
        return domain;
    }

    public BusinessAccountEntity getBusinessAccount() throws ServiceException {
        if (businessAccount != null)
            return businessAccount;
        loadDBU();
        if (businessAccount == null)
            throw new ServiceException(ErrorCodeTwins.BUSINESS_ACCOUNT_UNKNOWN);
        return businessAccount;
    }

    public UserEntity getUser() throws ServiceException {
        if (user != null)
            return user;
        loadDBU();
        if (user == null)
            throw new ServiceException(ErrorCodeTwins.USER_UNKNOWN);
        return user;
    }

    private void resolveDomainId() {
        if (domainId != null)
            return;
        if (domainResolver == null)
            domainResolver = domainResolverHeaders;
        try {
            domainId = domainResolver.resolveCurrentDomainId();
        } catch (ServiceException e) {
            log.error("Resolve domainId exception:", e);
        }
        if (domainId == null)
            domainId = NOT_SPECIFIED;
    }

    private void resolveBusinessAccountId() {
        if (businessAccountId != null)
            return;
        if (businessAccountResolver == null)
            businessAccountResolver = userBusinessAccountResolverAuthToken;
        try {
            businessAccountId = businessAccountResolver.resolveCurrentBusinessAccountId();
        } catch (ServiceException e) {
            log.error("Resolve businessAccountId exception:", e);
        }
        if (businessAccountId == null)
            businessAccountId = NOT_SPECIFIED;
    }

    private void resolveUserId() {
        if (userId != null)
            return;
        if (userResolver == null)
            userResolver = userBusinessAccountResolverAuthToken;
        try {
            userId = userResolver.resolveCurrentUserId();
        } catch (ServiceException e) {
            log.error("Resolve userId exception:", e);
        }
        if (userId == null)
            userId = NOT_SPECIFIED;
    }

    public boolean isDomainSpecified() {
        if (domain != null)
            return true;
        resolveDomainId();
        return domainId != null && !NOT_SPECIFIED.equals(businessAccountId);
    }

    public boolean isBusinessAccountSpecified() {
        if (businessAccount != null)
            return true;
        resolveBusinessAccountId();
        return businessAccountId != null && !NOT_SPECIFIED.equals(businessAccountId);
    }

    public boolean isUserSpecified() {
        if (user != null)
            return true;
        resolveUserId();
        return userId != null && !NOT_SPECIFIED.equals(userId);
    }

    public UUID getDomainId() throws ServiceException {
        if (isDomainSpecified())
            return getDomain().getId();
        return null;
    }

    public UUID getBusinessAccountId() throws ServiceException {
        if (isBusinessAccountSpecified())
            return getBusinessAccount().getId();
        return null;
    }

    public UUID getUserId() throws ServiceException {
        if (isUserSpecified())
            return getUser().getId();
        return null;
    }

    /**
     * DBU
     * D - domain
     * B - businessAccount
     * U - user
     *
     * @throws ServiceException
     */
    private void loadDBU() throws ServiceException {
        resolveDomainId();
        resolveBusinessAccountId();
        resolveUserId();
        if (checkMembershipMode) {
            if (isUserSpecified() && isDomainSpecified() && isBusinessAccountSpecified() && (domain == null || businessAccount == null || user == null)) {
                List<Object[]> dbuList = userRepository.findDBU_ByUserIdAndBusinessAccountIdAndDomainId(userId, businessAccountId, domainId);
                if (CollectionUtils.isEmpty(dbuList) || dbuList.size() != 1 || ArrayUtils.isEmpty(dbuList.get(0)) || dbuList.get(0).length != 3)
                    throw new ServiceException(ErrorCodeTwins.USER_UNKNOWN, "User[" + userId + "] is not registered in domain[" + domainId + "] or business account[" + businessAccountId + "]");
                domain = (DomainEntity) dbuList.get(0)[0];
                businessAccount = (BusinessAccountEntity) dbuList.get(0)[1];
                user = (UserEntity) dbuList.get(0)[2];
                return;
            } else if (isDomainSpecified() && isBusinessAccountSpecified() && (domain == null || businessAccount == null)) {
                DomainBusinessAccountEntity domainBusinessAccountEntity = domainBusinessAccountRepository.findByDomainIdAndBusinessAccountId(domainId, businessAccountId);
                if (domainBusinessAccountEntity == null)
                    throw new ServiceException(ErrorCodeTwins.USER_UNKNOWN, "Business account[" + businessAccountId + "] is not registered in domain[" + domainId + "]");
                domain = domainBusinessAccountEntity.getDomain();
                businessAccount = domainBusinessAccountEntity.getBusinessAccount();
                return;
            } else if (isDomainSpecified() && isUserSpecified() && (domain == null || user == null)) {
                DomainUserEntity domainUserEntity = domainUserRepository.findByDomainIdAndUserId(domainId, userId, DomainUserEntity.class);
                if (domainUserEntity == null)
                    throw new ServiceException(ErrorCodeTwins.USER_UNKNOWN, "User[" + userId + "] is not registered in domain[" + domainId + "]");
                domain = domainUserEntity.getDomain();
                user = domainUserEntity.getUser();
                return;
            } else if (isBusinessAccountSpecified() && isUserSpecified() && (businessAccount == null || user == null)) {
                BusinessAccountUserEntity businessAccountUserEntity = businessAccountUserRepository.findByBusinessAccountIdAndUserId(businessAccountId, userId, BusinessAccountUserEntity.class);
                if (businessAccountUserEntity == null)
                    throw new ServiceException(ErrorCodeTwins.USER_UNKNOWN, "User[" + userId + "] is not registered in business account[" + businessAccountId + "]");
                businessAccount = businessAccountUserEntity.getBusinessAccount();
                user = businessAccountUserEntity.getUser();
                return;
            }
        }
        if (isDomainSpecified() && domain == null)
            domain = entitySmartService.findById(domainId, domainRepository, EntitySmartService.FindMode.ifEmptyThrows);
        if (isBusinessAccountSpecified() && businessAccount == null)
            businessAccount = entitySmartService.findById(businessAccountId, businessAccountRepository, EntitySmartService.FindMode.ifEmptyThrows);
        if (isUserSpecified() && user == null)
            user = entitySmartService.findById(userId, userRepository, EntitySmartService.FindMode.ifEmptyThrows);
    }

    public Channel getChannel() {
        return Channel.WEB; //todo fix
    }

    public ApiUser setAnonymous(UUID domainId) {
        return setDomainResolver(new DomainResolverGivenId(domainId))
                .setUserResolver(new UserResolverNotSpecified())
                .setBusinessAccountResolver(new BusinessAccountResolverNotSpecified());
    }

    public ApiUser setAnonymous() {
        return setDomainResolver(domainResolverHeaders)
                .setUserResolver(new UserResolverNotSpecified())
                .setBusinessAccountResolver(new BusinessAccountResolverNotSpecified());
    }
}
