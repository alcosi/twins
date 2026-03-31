package org.twins.core.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.util.UuidUtils;
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
    private BusinessAccountEntity machineBusinessAccount;
    private UserEntity user;
    private UserEntity machineUser;
    private UUID domainId;
    private UUID businessAccountId;
    private UUID userId;
    private UUID machineBusinessAccountId;
    private UUID machineUserId;
    private DomainResolver domainResolver;
    private Locale locale;
    private LocaleResolver localeResolver;
    private BusinessAccountResolver businessAccountResolver;
    private UserResolver userResolver;
    private MachineBusinessAccountResolver machineBusinessAccountResolver;
    private MachineUserResolver machineUserResolver;
    private Channel channel;
    @Getter
    private ActAsUserStep actAsUserStep = ActAsUserStep.OMITTED;
    public static final UUID NOT_SPECIFIED = UuidUtils.NULLIFY_MARKER;

    @Getter
    private final UUID requestId = UuidUtils.generate();

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
        // do not change this code, because of security issues
        // if domain is already resolved, changing resolver should not make any sense
        this.domainResolver = domainResolver;
        if (NOT_SPECIFIED.equals(this.domainId))
            this.domainId = null;
        return this;
    }

    public ApiUser setBusinessAccountResolver(BusinessAccountResolver businessAccountResolver) {
        // do not change this code, because of security issues
        // if businessAccount is already resolved, changing resolver should not make any sense
        this.businessAccountResolver = businessAccountResolver;
        if (NOT_SPECIFIED.equals(this.businessAccountId))
            this.businessAccountId = null;
        return this;
    }

    public ApiUser setUserResolver(UserResolver userResolver) {
        // do not change this code, because of security issues
        // if user is already resolved, changing resolver should not make any sense
        this.userResolver = userResolver;
        if (NOT_SPECIFIED.equals(this.userId))
            this.userId = null;
        return this;
    }

    public ApiUser setMachineUserResolver(MachineUserResolver machineUserResolver) {
        this.machineUserResolver = machineUserResolver;
        if (NOT_SPECIFIED.equals(this.machineUserId))
            this.machineUserId = null;
        return this;
    }

    public ApiUser setMachineBusinessAccountResolver(MachineBusinessAccountResolver machineBusinessAccountResolver) {
        this.machineBusinessAccountResolver = machineBusinessAccountResolver;
        if (NOT_SPECIFIED.equals(this.machineBusinessAccountId))
            this.machineBusinessAccountId = null;
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

    public UserEntity getMachineUser() throws ServiceException {
        if (machineUser != null)
            return machineUser;
        loadDBU();
        if (machineUser == null)
            throw new ServiceException(ErrorCodeTwins.USER_UNKNOWN);
        return machineUser;
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
        log.info("resolved locale[{}]", locale);
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
        log.info("resolved domainId[{}]", domainId);
        if (domainId == null)
            domainId = NOT_SPECIFIED;
    }

    private void resolveBusinessAccountId() {
        if (businessAccountId != null)
            return;
        if (businessAccountResolver == null)
            businessAccountResolver = apiUserResolverService.getMainResolverAuthToken();
        try {
            businessAccountId = businessAccountResolver.resolveCurrentBusinessAccountId();
        } catch (ServiceException e) {
            log.error("Resolve businessAccountId exception:", e);
        }
        log.info("resolved businessAccountId[{}]", businessAccountId);
        if (businessAccountId == null)
            businessAccountId = NOT_SPECIFIED;
    }

    private void resolveMachineBusinessAccountId() {
        if (machineBusinessAccountId != null)
            return;
        if (machineBusinessAccountResolver == null)
            machineBusinessAccountResolver = apiUserResolverService.getMainResolverAuthToken();
        try {
            machineBusinessAccountId = machineBusinessAccountResolver.resolveMachineBusinessAccountId();
        } catch (ServiceException e) {
            log.error("Resolve machine businessAccountId exception:", e);
        }
        log.info("resolved machineBusinessAccountId[{}]", machineBusinessAccountId);
        if (machineBusinessAccountId == null)
            machineBusinessAccountId = NOT_SPECIFIED;
    }

    private void resolveUserId() throws ServiceException {
        if (userId != null)
            return;
        if (userResolver == null)
            userResolver = apiUserResolverService.getMainResolverAuthToken();
        userId = userResolver.resolveCurrentUserId();
        log.info("resolved userId[{}]", userId);
        if (userId == null)
            userId = NOT_SPECIFIED;
    }

    private void resolveMachineUserId() throws ServiceException {
        if (machineUserId != null)
            return;
        if (machineUserResolver == null)
            machineUserResolver = apiUserResolverService.getMainResolverAuthToken();
        machineUserId = machineUserResolver.resolveCurrentMachineUserId();
        log.info("resolved machineUserId[{}]", machineUserId);
        if (machineUserId == null)
            machineUserId = NOT_SPECIFIED;
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
    public boolean isUserSpecified() throws ServiceException {
        if (user != null)
            return true;
        resolveUserId();
        return userId != null && !NOT_SPECIFIED.equals(userId);
    }

    //this method only indicates that we have some data about domain id, but it's unchecked
    public boolean isMachineUserSpecified() throws ServiceException {
        if (machineUser != null)
            return true;
        resolveMachineUserId();
        return machineUserId != null && !NOT_SPECIFIED.equals(machineUserId);
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

    public UUID getMachineUserId() throws ServiceException {
        if (isMachineUserSpecified())
            return getMachineUser().getId();
        return null;
    }

    /**
     * DBU
     * D - domain
     * B - businessAccount
     * U - user
     * This method is very important to for checking membership
     *
     * @throws ServiceException
     */
    private void loadDBU() throws ServiceException {
        resolveDomainId();
        resolveBusinessAccountId();
        resolveUserId();
        resolveMachineUserId();
        resolveMachineBusinessAccountId();
        if (machineUserId != null && !NOT_SPECIFIED.equals(machineUserId)) {
            ApiUserResolverService.DBU dbu = new ApiUserResolverService.DBU(domain, machineBusinessAccount, machineUser); //all args can be null
            apiUserResolverService.loadDBU(domainId, machineBusinessAccountId, machineUserId, dbu, checkMembershipMode);
            domain = dbu.getDomain();
            machineBusinessAccount = dbu.getBusinessAccount();
            machineUser = dbu.getUser();
            setActAsUserStep(ActAsUserStep.PERMISSION_CHECK_NEEDED);
        }
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

    public void setActAsUserStep(ActAsUserStep nextActAsUserStep) throws ServiceException {
        if (nextActAsUserStep.step == (actAsUserStep.step + 1)) {
            actAsUserStep = nextActAsUserStep;
        }
        // log or throw new ServiceException(ErrorCodeCommon.UNEXPECTED_SERVER_EXCEPTION);
    }

    @RequiredArgsConstructor
    public enum ActAsUserStep {
        OMITTED(0),
        PERMISSION_CHECK_NEEDED(1),
        USER_GROUP_INVOLVE_NEEDED(2),
        READY(3);

        final int step;
    }
}
