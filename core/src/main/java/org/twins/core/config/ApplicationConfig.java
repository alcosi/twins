/*
 * Copyright (c)
 * created:2021 - 5 - 14
 * by Yan Tayanouski
 * ESAS Ltd. La propriété, c'est le vol!
 */

package org.twins.core.config;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.cambium.service.EntitySmartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.core.annotation.Order;
import org.springframework.core.task.TaskDecorator;
import org.springframework.core.task.TaskExecutor;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.SimpleAsyncTaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.client.RestTemplate;
import org.twins.core.config.filter.I18nCacheCleanupFilter;
import org.twins.core.config.filter.LoggingFilter;
import org.twins.core.config.filter.UncaughtExceptionFilter;
import org.twins.core.featurer.scheduler.Scheduler;

import javax.sql.DataSource;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
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
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(60000); // 1 min for connection setup
        factory.setReadTimeout(60000); // 1 min for reading data
        final RestTemplate restTemplate = new RestTemplate(new BufferingClientHttpRequestFactory(factory));
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

    @Order(2)
    @Bean(name = "uncaughtExceptionFilter", value = "uncaughtExceptionFilter")
    public UncaughtExceptionFilter uncaughtExceptionFilter(UncaughtExceptionFilter.LoggingController controller, ObjectMapper objectMapper) {
        return new UncaughtExceptionFilter(controller, objectMapper);
    }

    @Order(3)
    @Bean(name = "i18nCacheCleanupFilter", value = "i18nCacheCleanupFilter")
    public I18nCacheCleanupFilter i18nCacheCleanupFilter() {
        return new I18nCacheCleanupFilter();
    }
    /**
     * Configures a MeterRegistry with common tags applied to all metrics.
     * This method customizes the MeterRegistry by adding a common tag
     * "application" with the value "TWINS".
     * These common tags are applied
     * to every metric created in the application, allowing for consistent
     * tagging and easier identification of metrics.
     *
     * @return a customizer for MeterRegistry that applies common tags.
     */
    @Bean
    public MeterRegistryCustomizer<MeterRegistry> meterRegistry() {
        return (registry) -> registry.config().commonTags("application", "TWINS");}

     /**
      * Configures and provides a CacheManager bean using Caffeine as the caching provider.
      * The CacheManager is set with an initial capacity of 1000 entries and an expiration
      * policy of 5 minutes after a write operation.
      * Null cache entries are allowed.
      *
      * @return a configured instance of CaffeineCacheManager with the specified settings
      */
     @Bean
    public CacheManager cacheManager() {
        Caffeine caffeine = Caffeine.newBuilder()
                 .initialCapacity(1000)
            //    .refreshAfterWrite(5, TimeUnit.MINUTES)
                .expireAfterWrite(5, TimeUnit.MINUTES);
        CaffeineCacheManager caffeineCacheManager = new CaffeineCacheManager();
        caffeineCacheManager.setCaffeine(caffeine);
        caffeineCacheManager.setAllowNullValues(true);
        return caffeineCacheManager;
    }

//    @Bean
//    public CacheManager cacheManager() {
//        return new ConcurrentMapCacheManager() {
//            @Override
//            protected Cache createConcurrentMapCache(String name) {
//                return new ConcurrentMapCache(
//                        name,
//                        CacheBuilder.newBuilder()
//                                .expireAfterWrite(5, TimeUnit.MINUTES)
//                                .build().asMap(),
//                        true);
//            }
//        };
//    }

    @Bean
    public EntitySmartService entitySmartService() {
        EntitySmartService entitySmartService = new EntitySmartService();
                entitySmartService.setDaoPackages(new String[]{"org.twins.core.dao", "org.cambium.i18n.dao"});
        return entitySmartService;
    }

    @Bean
    public TaskExecutor draftCommitExecutor(@Autowired(required = false) TaskDecorator taskDecorator) {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5); //todo move to settings
        executor.setMaxPoolSize(10);
        executor.setThreadNamePrefix("draftCommitExecutor-");
        if (taskDecorator != null) executor.setTaskDecorator(taskDecorator);
        executor.initialize();
        return executor;
    }

    @Bean
    public TaskExecutor draftCollectEraseScopeExecutor(@Autowired(required = false) TaskDecorator taskDecorator) {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5); //todo move to settings
        executor.setMaxPoolSize(10);
        executor.setThreadNamePrefix("draftCollectEraseScopeExecutor-");
        if (taskDecorator != null) executor.setTaskDecorator(taskDecorator);
        executor.initialize();
        return executor;
    }

    @Bean
    public TaskExecutor twinChangeTaskExecutor(@Autowired(required = false) TaskDecorator taskDecorator) {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5); //todo move to settings
        executor.setMaxPoolSize(10);
        executor.setThreadNamePrefix("twinChangeTaskExecutor-");
        if (taskDecorator != null) executor.setTaskDecorator(taskDecorator);
        executor.initialize();
        return executor;
    }

    @Bean
    public TaskExecutor historyNotificationTaskExecutor(@Autowired(required = false) TaskDecorator taskDecorator) {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5); //todo move to settings
        executor.setMaxPoolSize(10);
        executor.setThreadNamePrefix("historyNotificationTaskExecutor-");
        if (taskDecorator != null) executor.setTaskDecorator(taskDecorator);
        executor.initialize();
        return executor;
    }

    @Bean(name = "attachmentDeleteTaskExecutor")
    public Executor attachmentDeleteTaskExecutor() {
        return Executors.newVirtualThreadPerTaskExecutor();
    }

    @Bean(name = "virtualThreadTaskScheduler")
    public TaskScheduler virtualThreadTaskScheduler(List<Scheduler> schedulerList) {
        var taskScheduler = new SimpleAsyncTaskScheduler();

        taskScheduler.setVirtualThreads(true);
        taskScheduler.setConcurrencyLimit(schedulerList.size() * 10);
        taskScheduler.setThreadNamePrefix("task-scheduler-vt-");

        return taskScheduler;
    }

    @Bean(name = "emailTaskExecutor")
    public Executor taskExecutor() {
        return Executors.newFixedThreadPool(10);
    }

//    @Bean(name = "cacheManagerRequestScope")
//    @RequestScope(proxyMode = ScopedProxyMode.TARGET_CLASS)
//    public CacheManager getCacheManager(){
//        return new ConcurrentMapCacheManager();
//    }
}