package org.twins.core.service.auth;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.springframework.stereotype.Service;
import org.twins.core.domain.ApiUser;
import org.twins.core.service.HttpRequestService;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {
    private final HttpRequestService httpRequestService;

    public ApiUser getApiUser() throws ServiceException {
        return new ApiUser()
                .userId(httpRequestService.getUserIdFromRequest())
                .businessAccountId(httpRequestService.getBusinessAccountIdFromRequest())
                .domainId(httpRequestService.getDomainIdFromRequest())
                .channel(httpRequestService.getChannelIdFromRequest());
    }
}
