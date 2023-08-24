package org.twins.core.service.auth;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.springframework.stereotype.Service;
import org.twins.core.domain.ApiUser;
import org.twins.core.domain.Channel;
import org.twins.core.service.HttpRequestService;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {
    private final HttpRequestService httpRequestService;

    public ApiUser getApiUser() throws ServiceException {
//        return new ApiUser()
//                .userId(httpRequestService.getUserIdFromRequest())
//                .businessAccountId(httpRequestService.getBusinessAccountIdFromRequest())
//                .domainId(httpRequestService.getDomainIdFromRequest())
//                .channel(httpRequestService.getChannelIdFromRequest());
        return new ApiUser()
                .userId(UUID.fromString("608c6d7d-99c8-4d87-89c6-2f72d0f5d673"))
                .businessAccountId(UUID.fromString("9a3f6075-f175-41cd-a804-934201ec969c"))
                .domainId(UUID.fromString("f67ad556-dd27-4871-9a00-16fb0e8a4102"))
                .channel(Channel.WEB);
    }
}
