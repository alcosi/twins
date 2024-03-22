package org.twins.core.domain.apiuser;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
@RequiredArgsConstructor
public class LocaleResolverHeader implements LocaleResolver {
    @Override
    public Locale resolveCurrentLocale() {
        //todo needs to be implemented correctly
        return Locale.ENGLISH;
    }
}
