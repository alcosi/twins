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
    protected Locale resolveCurrentUserLocale() throws ServiceException {
        ApiUser apiUser = authService.getApiUser();
        return apiUser.getLocale();
    }
}
