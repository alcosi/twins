package org.twins.core.config;

import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.twins.core.config.jackson.I18nValueSerializer;

@Configuration
public class JacksonConfig {
    @Bean
    public Module i18nModule() {
        SimpleModule module = new SimpleModule();
        module.addSerializer(String.class, new I18nValueSerializer());
        return module;
    }
}