package org.twins.core.domain.apiuser;

import lombok.RequiredArgsConstructor;
import org.cambium.common.exception.ServiceException;
import org.springframework.stereotype.Component;
import org.twins.core.domain.ApiUser;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.user.UserService;

import java.util.Locale;

@Component
@RequiredArgsConstructor
public class LocaleResolverDomainUser implements LocaleResolver {
    final AuthService authService;
    final UserService userService;

    @Override
    public Locale resolveCurrentLocale() throws ServiceException {
        ApiUser apiUser = authService.getApiUser();
        return userService.getUserLocale(apiUser.getDomainId(), apiUser.getUserId());
    }
}
