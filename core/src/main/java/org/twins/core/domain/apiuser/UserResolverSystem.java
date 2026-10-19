package org.twins.core.domain.apiuser;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.springframework.stereotype.Component;
import org.twins.core.enums.consts.SystemIds;

import java.util.UUID;

@Component
@Slf4j
public class UserResolverSystem implements UserResolver {
    UserResolverGivenId userResolverGivenId;

    @Override
    public UUID resolveCurrentUserId() throws ServiceException {
        if (userResolverGivenId == null)
            userResolverGivenId = new UserResolverGivenId(SystemIds.User.SYSTEM);
        return userResolverGivenId.resolveCurrentUserId();
    }
}
