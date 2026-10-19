package org.twins.core.domain.apiuser;

import lombok.RequiredArgsConstructor;
import org.cambium.common.exception.ServiceException;
import org.springframework.stereotype.Component;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.domain.DomainService;

import java.util.Locale;

@Component
@RequiredArgsConstructor
public class LocaleResolverDomainUser implements LocaleResolver {
    final DomainService domainService;
    final AuthService authService;

    @Override
    public Locale resolveCurrentLocale() throws ServiceException {
        Locale locale;
        var apiUser = authService.getApiUser();
        if (apiUser.isSystemUser()) {
            locale = domainService.getDefaultDomainLocale(apiUser.getDomainId());
        } else {
            var domainUser = apiUser.getDomainUser();
            if (domainUser.getI18nLocaleId() != null)
                locale = domainUser.getI18nLocaleId();
            else
                locale = apiUser.getDomain().getDefaultI18nLocaleId();
        }
        return locale;
    }
}
