package org.twins.core.i18n.config;

import org.springframework.context.annotation.Configuration;

import java.util.Locale;

@Configuration
public interface I18nProperties {
    public Locale defaultLocale();
}
