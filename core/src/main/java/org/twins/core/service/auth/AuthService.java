package org.twins.core.service.auth;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.springframework.stereotype.Service;
import org.twins.core.dao.businessaccount.BusinessAccountRepository;
import org.twins.core.dao.domain.DomainRepository;
import org.twins.core.dao.user.UserRepository;
import org.twins.core.domain.ApiUser;
import org.twins.core.domain.Channel;
import org.twins.core.service.EntitySmartService;
import org.twins.core.service.HttpRequestService;


@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {
    private final HttpRequestService httpRequestService;
    private final UserRepository userRepository;
    private final BusinessAccountRepository businessAccountRepository;
    private final DomainRepository domainRepository;
    private final EntitySmartService entitySmartService;

    public ApiUser getApiUser() throws ServiceException {
        return getApiUser(
                EntitySmartService.CheckMode.NOT_EMPTY_AND_DB_EXISTS,
                EntitySmartService.CheckMode.NOT_EMPTY_AND_DB_EXISTS                ,
                EntitySmartService.CheckMode.NOT_EMPTY_AND_DB_EXISTS);
    }

    public ApiUser getApiUser(EntitySmartService.CheckMode userCheck, EntitySmartService.CheckMode businessAccountCheck, EntitySmartService.CheckMode domainCheck) throws ServiceException {
        String userId = httpRequestService.getUserIdFromRequest();
        String businessAccountId = httpRequestService.getBusinessAccountIdFromRequest();
        String domainId = httpRequestService.getDomainIdFromRequest();
        String channel = httpRequestService.getChannelIdFromRequest();

//        String userId = "608c6d7d-99c8-4d87-89c6-2f72d0f5d673";
//        String businessAccountId = "9a3f6075-f175-41cd-a804-934201ec969c";
//        String domainId = "f67ad556-dd27-4871-9a00-16fb0e8a4102";
//        String channel = "WEB";

        return new ApiUser()
                .userId(entitySmartService.check(userId, "userId", userRepository, userCheck))
                .businessAccountId(entitySmartService.check(businessAccountId, "businessAccountId", businessAccountRepository, businessAccountCheck))
                .domainId(entitySmartService.check(domainId, "domainId", domainRepository, domainCheck))
                .channel(Channel.resolve(channel));
    }

}
