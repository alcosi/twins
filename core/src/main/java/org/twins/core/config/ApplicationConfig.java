/*
 * Copyright (c)
 * created:2021 - 5 - 14
 * by Yan Tayanouski
 * ESAS Ltd. La propriété, c'est le vol!
 */

package org.twins.core.config;


import com.google.common.cache.CacheBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.core.annotation.Order;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.web.client.RestTemplate;
import org.twins.core.config.filter.LoggingFilter;

import javax.sql.DataSource;
import java.util.concurrent.TimeUnit;

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

    @Bean
    public LoggingFilter.LogInternalService logInternalService() {
        return new LoggingFilter.LogInternalService();
    }

    @Order(1)
    @Bean(name = "loggingFilterBean", value = "loggingFilterBean")
    public LoggingFilter loggingFilter() {
        return new LoggingFilter();
    }

    @Bean
    public CacheManager cacheManager() {
        return new ConcurrentMapCacheManager() {
            @Override
            protected Cache createConcurrentMapCache(String name) {
                return new ConcurrentMapCache(
                        name,
                        CacheBuilder.newBuilder()
                                .expireAfterWrite(5, TimeUnit.MINUTES)
                                .build().asMap(),
                        true);
            }
        };
    }

//    @Bean(name = "cacheManagerRequestScope")
//    @RequestScope(proxyMode = ScopedProxyMode.TARGET_CLASS)
//    public CacheManager getCacheManager(){
//        return new ConcurrentMapCacheManager();
//    }
}