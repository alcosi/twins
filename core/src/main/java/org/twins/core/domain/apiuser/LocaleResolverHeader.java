package org.twins.core.domain.apiuser;

import lombok.RequiredArgsConstructor;
import org.cambium.common.exception.ServiceException;
import org.springframework.stereotype.Component;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.service.HttpRequestService;

import java.util.Locale;

@Component
@RequiredArgsConstructor
public class LocaleResolverHeader implements LocaleResolver {
    final HttpRequestService httpRequestService;

    @Override
    public Locale resolveCurrentLocale() throws ServiceException {
        Locale locale;
        try {
            locale = httpRequestService.getLocale();
        } catch (Exception e) {
            throw new ServiceException(ErrorCodeTwins.USER_LOCALE_UNKNOWN);
        }
        return locale;
    }
}
