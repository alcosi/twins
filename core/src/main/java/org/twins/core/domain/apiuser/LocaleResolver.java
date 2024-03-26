package org.twins.core.domain.apiuser;

import org.cambium.common.exception.ServiceException;

import java.util.Locale;

public interface LocaleResolver {
    Locale resolveCurrentLocale() throws ServiceException;
}
