package org.twins.core.featurer.tokenhandler;

import lombok.RequiredArgsConstructor;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.springframework.stereotype.Component;
import org.twins.core.domain.ApiUser;
import org.twins.core.service.EntitySmartService;
import org.twins.core.service.businessaccount.BusinessAccountService;
import org.twins.core.service.user.UserService;

import java.util.Properties;
import java.util.UUID;

@Component
@Featurer(id = 1901,
        name = "TokenHandlerStub",
        description = "")
@RequiredArgsConstructor
public class TokenHandlerStub extends TokenHandler {
    final UserService userService;
    final BusinessAccountService businessAccountService;

    @Override
    protected ApiUser resolveApiUser(Properties properties, String token) throws ServiceException {
        String[] tokenData = token.split(",");
        ApiUser apiUser = new ApiUser().setUser(userService.findByUserId(UUID.fromString(tokenData[0].trim()), EntitySmartService.FindMode.ifEmptyThrows));
        if (tokenData.length > 1)
            apiUser.setBusinessAccount(businessAccountService.findById(UUID.fromString(tokenData[1].trim()), EntitySmartService.FindMode.ifEmptyThrows));
        return apiUser;
    }
}
