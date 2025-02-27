package org.twins.core.config.i18n;

import org.springframework.context.annotation.Configuration;

import java.util.Locale;

@Configuration
public interface I18nProperties {
    public Locale defaultLocale();
}
