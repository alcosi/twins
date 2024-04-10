package org.twins.core.service.locale;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.i18n.service.I18nService;
import org.springframework.stereotype.Service;
import org.twins.core.domain.ApiUser;
import org.twins.core.service.auth.AuthService;

import java.util.Locale;


@Service
@RequiredArgsConstructor
@Slf4j
public class I18nTwinsService extends I18nService {

    final AuthService authService;
    @Override
    protected Locale resolveCurrentUserLocale() {
        ApiUser apiUser;
        try {
            apiUser = authService.getApiUser();
            return apiUser.getLocale();
        } catch (ServiceException e) {
            return null;
        }
    }

    @Override
    protected Locale resolveDefaultLocale() {
        ApiUser apiUser;
        try {
            apiUser = authService.getApiUser();
            Locale domainLocale = apiUser.getDomain().getDefaultI18nLocaleId();
            return domainLocale != null ? domainLocale : super.resolveDefaultLocale();
        } catch (ServiceException e) {
            return super.resolveDefaultLocale();
        }
    }
}
