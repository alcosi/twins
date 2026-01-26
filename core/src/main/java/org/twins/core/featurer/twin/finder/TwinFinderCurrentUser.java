package org.twins.core.featurer.twin.finder;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.twins.core.service.auth.AuthService;

import java.util.UUID;

@Slf4j
public abstract class TwinFinderCurrentUser extends TwinFinder {
    @Autowired
    @Lazy
    AuthService authService;

    public UUID getCurrentUserId() throws ServiceException {
        return authService.getApiUser().getUserId();
    }
}
