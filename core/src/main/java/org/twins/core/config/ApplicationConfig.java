/*
 * Copyright (c)
 * created:2021 - 5 - 14
 * by Yan Tayanouski
 * ESAS Ltd. La propriété, c'est le vol!
 */

package org.twins.core.config;


import lombok.extern.slf4j.Slf4j;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.*;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.client.RestTemplate;
import javax.sql.DataSource;

import static org.springframework.context.annotation.ComponentScan.Filter;

@Slf4j
@Configuration
//@ComponentScan(basePackages = {"org.twins", "org.cambium"}, excludeFilters = {
//        @Filter({Controller.class, Configuration.class})})
@PropertySources({
        @PropertySource(value = "classpath:/application.properties", ignoreResourceNotFound = true)})
@EnableConfigurationProperties({I18nProperties.class})
public class ApplicationConfig {
    @Bean
    public NamedParameterJdbcTemplate namedParameterJdbcTemplate(DataSource dataSource) {
        return new NamedParameterJdbcTemplate(dataSource);

    }

    @Bean
    public RestTemplate restTemplate(RestTemplateConfig.LogRequestResponseFilter filter) {
        final RestTemplate restTemplate = new RestTemplate(new BufferingClientHttpRequestFactory(new SimpleClientHttpRequestFactory()));
        restTemplate.getInterceptors().add(filter);
        return restTemplate;
    }

    @Bean
    public MessageSource apiMessageSource() {
        ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
        messageSource.setBasename("classpath:api_messages");
        messageSource.setCacheSeconds(10); //reload messages every 10 seconds
        messageSource.setDefaultEncoding("UTF-8");
        return messageSource;
    }

}