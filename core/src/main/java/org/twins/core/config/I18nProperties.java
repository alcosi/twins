package org.twins.core.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;

import java.util.Locale;

@PropertySource("classpath:configprops.properties")
@ConfigurationProperties(prefix = "cambium.i18n")
@Data
public class I18nProperties implements org.twins.core.config.i18n.I18nProperties {
    private String defaultLocale;

    @Override
    public Locale defaultLocale() {
        return Locale.forLanguageTag(defaultLocale);
    }
}
