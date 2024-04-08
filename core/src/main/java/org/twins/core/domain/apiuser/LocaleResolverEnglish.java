package org.twins.core.domain.apiuser;

import org.cambium.common.exception.ServiceException;

import java.util.Locale;

public class LocaleResolverEnglish implements LocaleResolver{
    @Override
    public Locale resolveCurrentLocale() throws ServiceException {
        return Locale.ENGLISH;
    }
}
