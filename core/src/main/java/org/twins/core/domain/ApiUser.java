package org.twins.core.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;
import org.twins.core.dao.businessaccount.BusinessAccountEntity;
import org.twins.core.dao.domain.DomainEntity;
import org.twins.core.dao.user.UserEntity;
import org.twins.core.domain.apiuser.*;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.service.auth.ApiUserResolverService;

import java.util.Collections;
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
    private Locale locale;
    private LocaleResolver localeResolver;
    private BusinessAccountResolver businessAccountResolver;
    private UserResolver userResolver;
    private Channel channel;
    public static final UUID NOT_SPECIFIED = UUID.fromString("ffffffff-ffff-ffff-ffff-ffffffffffff");

    @Getter
    private final UUID requestId = UUID.randomUUID();

    public Set<UUID> getPermissions() {
        if (user != null && user.getPermissions() != null)
            return user.getPermissions();
        else
            return Collections.EMPTY_SET;
    }

    @Getter
    @Setter
    private boolean checkMembershipMode = true;

    final ApiUserResolverService apiUserResolverService;

    public ApiUser setDomainResolver(DomainResolver domainResolver) {
        this.domainResolver = domainResolver;
        if (NOT_SPECIFIED.equals(this.domainId))
            this.domainId = null;
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
        //we do not call loadDBU because we need domain to detect identity connector to resolve userId and businessAccountId
        //otherwise we will get endless loop
        resolveDomainId();
        if (domainId == null)
            throw new ServiceException(ErrorCodeTwins.DOMAIN_UNKNOWN);
        domain = apiUserResolverService.findDomain(domainId);
        return domain;
    }


    public Locale getLocale() throws ServiceException {
        if (locale != null)
            return locale;
        resolveLocale();
        if (locale == null)
            throw new ServiceException(ErrorCodeTwins.USER_LOCALE_UNKNOWN);
        return locale;
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

    private void resolveLocale() {
        if (locale != null)
            return;
        if (localeResolver == null)
            localeResolver = apiUserResolverService.getLocaleResolverDomainUser();
        try {
            locale = localeResolver.resolveCurrentLocale();
        } catch (ServiceException e) {
            log.error("Resolve locale exception:", e);
        }
    }

    private void resolveDomainId() {
        if (domainId != null)
            return;
        if (domainResolver == null)
            domainResolver = apiUserResolverService.getDomainResolverHeaders();
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
            businessAccountResolver = apiUserResolverService.getUserBusinessAccountResolverAuthToken();
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
            userResolver = apiUserResolverService.getUserBusinessAccountResolverAuthToken();
        try {
            userId = userResolver.resolveCurrentUserId();
        } catch (ServiceException e) {
            log.error("Resolve userId exception:", e);
        }
        if (userId == null)
            userId = NOT_SPECIFIED;
    }

    //this method only indicates that we have some data about domain id, but it's unchecked
    public boolean isDomainSpecified() {
        if (domain != null)
            return true;
        resolveDomainId();
        return domainId != null && !NOT_SPECIFIED.equals(domainId);
    }

    //this method only indicates that we have some data about domain id, but it's unchecked
    public boolean isBusinessAccountSpecified() {
        if (businessAccount != null)
            return true;
        resolveBusinessAccountId();
        return businessAccountId != null && !NOT_SPECIFIED.equals(businessAccountId);
    }

    //this method only indicates that we have some data about domain id, but it's unchecked
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
     * This method is very important to for checking membership
     * @throws ServiceException
     */
    private void loadDBU() throws ServiceException {
        resolveDomainId();
        resolveBusinessAccountId();
        resolveUserId();

        ApiUserResolverService.DBU dbu = new ApiUserResolverService.DBU(domain, businessAccount, user); //all args can be null
        apiUserResolverService.loadDBU(domainId, businessAccountId, userId, dbu, checkMembershipMode);
        domain = dbu.getDomain();
        businessAccount = dbu.getBusinessAccount();
        user = dbu.getUser();
    }

    public Channel getChannel() {
        return Channel.WEB; //todo fix
    }

    public ApiUser setAnonymous(UUID domainId) {
        return setDomainResolver(new DomainResolverGivenId(domainId))
                .setUserResolver(new UserResolverNotSpecified())
                .setLocaleResolver(apiUserResolverService.getLocaleResolverHeader())
                .setBusinessAccountResolver(new BusinessAccountResolverNotSpecified());
    }

    public ApiUser setAnonymous() {
        return setDomainResolver(apiUserResolverService.getDomainResolverHeaders())
                .setUserResolver(new UserResolverNotSpecified())
                .setLocaleResolver(apiUserResolverService.getLocaleResolverHeader())
                .setBusinessAccountResolver(new BusinessAccountResolverNotSpecified());
    }

    public ApiUser setAnonymousWithDefaultLocale() {
        return setDomainResolver(apiUserResolverService.getDomainResolverHeaders())
                .setUserResolver(new UserResolverNotSpecified())
                .setLocaleResolver(new LocaleResolverEnglish())
                .setBusinessAccountResolver(new BusinessAccountResolverNotSpecified());
    }
}
