package org.twins.core.service.auth;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.FeaturerService;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.twins.core.dao.businessaccount.BusinessAccountRepository;
import org.twins.core.dao.domain.DomainEntity;
import org.twins.core.dao.domain.DomainRepository;
import org.twins.core.dao.user.UserRepository;
import org.twins.core.domain.ApiUser;
import org.twins.core.domain.Channel;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.tokenhandler.TokenHandler;
import org.twins.core.service.EntitySmartService;
import org.twins.core.service.HttpRequestService;
import org.twins.core.service.domain.DomainService;

import java.util.UUID;


@Slf4j
@Service
@Lazy
@RequiredArgsConstructor
public class AuthService {
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

    public ApiUser getApiUser() throws ServiceException {
        return apiUser;
    }
}
