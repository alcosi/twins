package org.twins.core.featurer.tokenhandler;

import lombok.RequiredArgsConstructor;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.springframework.stereotype.Component;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.service.businessaccount.BusinessAccountService;
import org.twins.core.service.user.UserService;

import java.util.Properties;
import java.util.UUID;

@Component
@Featurer(id = FeaturerTwins.ID_1901,
        name = "TokenHandlerStub",
        description = "")
@RequiredArgsConstructor
public class TokenHandlerStub extends TokenHandler {
    final UserService userService;
    final BusinessAccountService businessAccountService;

    @Override
    protected Result resolveUserIdAndBusinessAccountId(Properties properties, String token) throws ServiceException {
        String[] tokenData = token.split(",");
        Result ret = new Result()
                .setUserId(UUID.fromString(tokenData[0].trim()));
        if (tokenData.length > 1)
            ret.setBusinessAccountId(UUID.fromString(tokenData[1].trim()));
        return ret;
    }
}
