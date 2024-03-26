package org.twins.core.domain.apiuser;

import lombok.RequiredArgsConstructor;
import org.cambium.common.exception.ServiceException;
import org.springframework.stereotype.Component;
import org.twins.core.dao.domain.DomainUserEntity;
import org.twins.core.service.domain.DomainService;

import java.util.Locale;

@Component
@RequiredArgsConstructor
public class LocaleResolverDomainUser implements LocaleResolver {
    final DomainService domainService;

    @Override
    public Locale resolveCurrentLocale() throws ServiceException {
        Locale locale;
        DomainUserEntity domainUser = domainService.getDomainUser();
        if (domainUser.getI18nLocaleId() != null)
            locale = domainUser.getI18nLocaleId();
        else {
            locale = domainService.getDefaultDomainLocale(domainUser.getDomainId());
        }
        return locale;
    }
}
