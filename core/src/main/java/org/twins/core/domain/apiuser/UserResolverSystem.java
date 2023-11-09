package org.twins.core.domain.apiuser;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.twins.core.service.SystemEntityService;

import java.util.UUID;

@Component
@Slf4j
public class UserResolverSystem implements UserResolver {
    @Autowired
    SystemEntityService systemEntityService;

    UserResolverGivenId userResolverGivenId;

    @Override
    public UUID resolveCurrentUserId() throws ServiceException {
        if (userResolverGivenId == null)
            userResolverGivenId = new UserResolverGivenId(systemEntityService.getUserIdSystem());
        return userResolverGivenId.resolveCurrentUserId();
    }
}
