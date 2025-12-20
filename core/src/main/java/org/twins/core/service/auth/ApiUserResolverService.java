package org.twins.core.service.auth;

import io.github.breninsul.logging.aspect.JavaLoggingLevel;
import io.github.breninsul.logging.aspect.annotation.LogExecutionTime;
import lombok.*;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.util.CollectionUtils;
import org.cambium.service.EntitySmartService;
import org.springframework.stereotype.Service;
import org.twins.core.dao.businessaccount.BusinessAccountEntity;
import org.twins.core.dao.businessaccount.BusinessAccountRepository;
import org.twins.core.dao.businessaccount.BusinessAccountUserEntity;
import org.twins.core.dao.businessaccount.BusinessAccountUserRepository;
import org.twins.core.dao.domain.*;
import org.twins.core.dao.user.UserEntity;
import org.twins.core.dao.user.UserRepository;
import org.twins.core.domain.apiuser.DomainResolverHeaders;
import org.twins.core.domain.apiuser.LocaleResolverDomainUser;
import org.twins.core.domain.apiuser.LocaleResolverHeader;
import org.twins.core.domain.apiuser.MainResolverAuthToken;
import org.twins.core.exception.ErrorCodeTwins;

import java.util.List;
import java.util.UUID;

import static org.twins.core.domain.ApiUser.NOT_SPECIFIED;

@Service
@LogExecutionTime(logPrefix = "LONG EXECUTION TIME:", logIfTookMoreThenMs = 2 * 1000, level = JavaLoggingLevel.WARNING)
@RequiredArgsConstructor
@Slf4j
public class ApiUserResolverService {
    final EntitySmartService entitySmartService;
    final DomainRepository domainRepository;
    final DomainUserRepository domainUserRepository;
    final DomainBusinessAccountRepository domainBusinessAccountRepository;
    final BusinessAccountRepository businessAccountRepository;
    final BusinessAccountUserRepository businessAccountUserRepository;
    final UserRepository userRepository;
    @Getter
    final DomainResolverHeaders domainResolverHeaders;
    @Getter
    final LocaleResolverDomainUser localeResolverDomainUser;
    @Getter
    final LocaleResolverHeader localeResolverHeader;
    @Getter
    final MainResolverAuthToken mainResolverAuthToken;

    public DomainEntity findDomain(UUID domainId) throws ServiceException {
        return entitySmartService.findById(domainId, domainRepository, EntitySmartService.FindMode.ifEmptyThrows);
    }

    public BusinessAccountEntity findBusinessAccount(UUID businessAccountId) throws ServiceException {
        return entitySmartService.findById(businessAccountId, businessAccountRepository, EntitySmartService.FindMode.ifEmptyThrows);
    }

    public UserEntity findUser(UUID userId) throws ServiceException {
        return entitySmartService.findById(userId, userRepository, EntitySmartService.FindMode.ifEmptyThrows);
    }

    public void checkDBU(UUID domainId, UUID businessAccountId, UUID userId) throws ServiceException {
        loadDBU(domainId, businessAccountId, userId, new DBU(), true);
    }

    public void loadDBU(UUID domainId, UUID businessAccountId, UUID userId, DBU dbu, boolean checkMembershipMode) throws ServiceException {
        if (checkMembershipMode) {
            if (isUserSpecified(userId) && isDomainSpecified(domainId) && isBusinessAccountSpecified(businessAccountId) && (dbu.getDomain() == null || dbu.getBusinessAccount() == null || dbu.getUser() == null)) {
                List<Object[]> dbuList = userRepository.findDBU_ByUserIdAndBusinessAccountIdAndDomainId(userId, businessAccountId, domainId);
                if (CollectionUtils.isEmpty(dbuList) || dbuList.size() != 1 || ArrayUtils.isEmpty(dbuList.get(0)) || dbuList.get(0).length != 3)
                    throw new ServiceException(ErrorCodeTwins.USER_UNKNOWN, "User[" + userId + "] is not registered in domain[" + domainId + "] or business account[" + businessAccountId + "]");
                dbu
                        .setDomain((DomainEntity) dbuList.get(0)[0])
                        .setBusinessAccount((BusinessAccountEntity) dbuList.get(0)[1])
                        .setUser((UserEntity) dbuList.get(0)[2]);
                return;
            } else if (isDomainSpecified(domainId) && isBusinessAccountSpecified(businessAccountId) && (dbu.getDomain() == null || dbu.getBusinessAccount() == null)) {
                DomainBusinessAccountEntity domainBusinessAccountEntity = domainBusinessAccountRepository.findByDomainIdAndBusinessAccountId(domainId, businessAccountId);
                if (domainBusinessAccountEntity == null)
                    throw new ServiceException(ErrorCodeTwins.USER_UNKNOWN, "Business account[" + businessAccountId + "] is not registered in domain[" + domainId + "]");
                dbu
                        .setDomain(domainBusinessAccountEntity.getDomain())
                        .setBusinessAccount(domainBusinessAccountEntity.getBusinessAccount());
                return;
            } else if (isDomainSpecified(domainId) && isUserSpecified(userId) && (dbu.getDomain() == null || dbu.getUser() == null)) {
                DomainUserNoCollectionProjection domainUserEntity = domainUserRepository.findByDomainIdAndUserId(domainId, userId, DomainUserNoCollectionProjection.class);
                if (domainUserEntity == null)
                    throw new ServiceException(ErrorCodeTwins.USER_UNKNOWN, "User[" + userId + "] is not registered in domain[" + domainId + "]");
                dbu
                        .setDomain(domainUserEntity.domain())
                        .setUser(domainUserEntity.user());
                return;
            } else if (isBusinessAccountSpecified(businessAccountId) && isUserSpecified(userId) && (dbu.getBusinessAccount() == null || dbu.getUser() == null)) {
                BusinessAccountUserEntity businessAccountUserEntity = businessAccountUserRepository.findByBusinessAccountIdAndUserId(businessAccountId, userId, BusinessAccountUserEntity.class);
                if (businessAccountUserEntity == null)
                    throw new ServiceException(ErrorCodeTwins.USER_UNKNOWN, "User[" + userId + "] is not registered in business account[" + businessAccountId + "]");
                dbu
                        .setBusinessAccount(businessAccountUserEntity.getBusinessAccount())
                        .setUser(businessAccountUserEntity.getUser());
                return;
            }
        }
        if (isDomainSpecified(domainId) && dbu.getDomain() == null)
            dbu.setDomain(findDomain(domainId));
        if (isBusinessAccountSpecified(businessAccountId) && dbu.getBusinessAccount() == null)
            dbu.setBusinessAccount(findBusinessAccount(businessAccountId));
        if (isUserSpecified(userId) && dbu.getUser() == null)
            dbu.setUser(findUser(userId));
    }

    private boolean isBusinessAccountSpecified(UUID businessAccountId) {
        return businessAccountId != null && !NOT_SPECIFIED.equals(businessAccountId);
    }

    private boolean isDomainSpecified(UUID domainId) {
        return domainId != null && !NOT_SPECIFIED.equals(domainId);
    }

    private boolean isUserSpecified(UUID userId) {
        return userId != null && !NOT_SPECIFIED.equals(userId);
    }

    @Data
    @Accessors(chain = true)
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DBU {
        private DomainEntity domain;
        private BusinessAccountEntity businessAccount;
        private UserEntity user;
    }
}
