package org.twins.core.domain.apiuser;

import java.util.Locale;

public class LocaleResolverHeader implements LocaleResolver{
    @Override
    public Locale resolveCurrentLocale() {
        return Locale.ENGLISH;
    }
}
