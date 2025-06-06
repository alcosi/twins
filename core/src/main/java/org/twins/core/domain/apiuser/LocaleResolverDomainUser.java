package org.twins.core.domain.apiuser;

import lombok.RequiredArgsConstructor;
import org.cambium.common.exception.ServiceException;
import org.springframework.stereotype.Component;
import org.twins.core.dao.domain.DomainUserEntity;
import org.twins.core.service.domain.DomainService;
import org.twins.core.service.domain.DomainUserService;

import java.util.Locale;

@Component
@RequiredArgsConstructor
public class LocaleResolverDomainUser implements LocaleResolver {
    final DomainService domainService;
    final DomainUserService domainUserService;

    @Override
    public Locale resolveCurrentLocale() throws ServiceException {
        Locale locale;
        DomainUserEntity domainUser = domainUserService.getDomainUser();
        if (domainUser.getI18nLocaleId() != null)
            locale = domainUser.getI18nLocaleId();
        else
            locale = domainService.getDefaultDomainLocale(domainUser.getDomainId());
        return locale;
    }
}
