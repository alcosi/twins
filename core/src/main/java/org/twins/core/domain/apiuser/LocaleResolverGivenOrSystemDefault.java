package org.twins.core.domain.apiuser;

import org.cambium.common.exception.ServiceException;

import java.util.Locale;


public class LocaleResolverGivenOrSystemDefault implements LocaleResolver {
    private Locale locale;

    public LocaleResolverGivenOrSystemDefault(String langTag) {
        try {
            locale = Locale.forLanguageTag(langTag);
        } catch (Exception e) {
            locale = Locale.ENGLISH; //todo get from properties
        }
    }

    @Override
    public Locale resolveCurrentLocale() throws ServiceException {
        return locale;
    }
}
