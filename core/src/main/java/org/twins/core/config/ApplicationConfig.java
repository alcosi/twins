/*
 * Copyright (c)
 * created:2021 - 5 - 14
 * by Yan Tayanouski
 * ESAS Ltd. La propriété, c'est le vol!
 */

package org.twins.core.config;


import io.github.resilience4j.bulkhead.BulkheadRegistry;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.cambium.service.EntitySmartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.micrometer.metrics.autoconfigure.MeterRegistryCustomizer;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.core.annotation.Order;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskDecorator;
import org.springframework.core.task.TaskExecutor;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.SimpleAsyncTaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.client.RestTemplate;
import org.twins.core.config.filter.BulkheadFilter;
import org.twins.core.config.filter.I18nCacheCleanupFilter;
import org.twins.core.config.filter.LoggingFilter;
import org.twins.core.config.filter.UncaughtExceptionFilter;
import org.twins.core.featurer.scheduler.Scheduler;
import tools.jackson.databind.ObjectMapper;

import javax.sql.DataSource;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;


@Slf4j
@Configuration
//@ComponentScan(basePackages = {"org.twins", "org.cambium"}, excludeFilters = {
//        @Filter({Controller.class, Configuration.class})})
@PropertySources({
        @PropertySource(value = "classpath:/application.properties", ignoreResourceNotFound = true)})
@EnableConfigurationProperties({I18nProperties.class})
public class ApplicationConfig {

    /**
     * Scheduler concurrency is sized against the DB connection pool so the pool is never the
     * bottleneck: the aggregate of all runners' maxPoolSize + trigger concurrency + virtual
     * executors should stay below ~0.8 * dbPoolSize. Every value below is overridable per environment.
     */
    @Value("${spring.datasource.hikari.maximum-pool-size:50}")
    private int dbPoolSize;
    @Value("${twins.scheduler.task-executor.core-pool-size:5}")
    private int schedulerTaskCorePoolSize;
    @Value("${twins.scheduler.task-executor.max-pool-size:10}")
    private int schedulerTaskMaxPoolSize;
    @Value("${twins.scheduler.task-executor.queue-capacity:100}")
    private int schedulerTaskQueueCapacity;
    @Value("${twins.scheduler.virtual-executor-concurrency:10}")
    private int virtualExecutorConcurrency;
    /** Scheduler trigger concurrency. Negative (default) = auto-derive from dbPoolSize. */
    @Value("${twins.scheduler.trigger-concurrency:-1}")
    private int schedulerTriggerConcurrency;

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

    @Order(4)
    @Bean(name = "bulkheadFilter", value = "bulkheadFilter")
    public BulkheadFilter bulkheadFilter(BulkheadRegistry bulkheadRegistry, RateLimiterRegistry rateLimiterRegistry) {
        return new BulkheadFilter(bulkheadRegistry, rateLimiterRegistry);
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
        return (registry) -> registry.config().commonTags("application", "TWINS");
    }

    @Bean
    public EntitySmartService entitySmartService() {
        EntitySmartService entitySmartService = new EntitySmartService();
        entitySmartService.setDaoPackages(new String[]{"org.twins.core.dao", "org.cambium.i18n.dao"});
        return entitySmartService;
    }

    /**
     * Every scheduler runner shares the same bounded shape (see {@link #buildSchedulerTaskExecutor}):
     * capped threads + capped queue + {@link ThreadPoolExecutor.AbortPolicy}. On overflow the
     * submission is rejected, and {@link org.twins.core.featurer.scheduler.SchedulerTaskRunner#processTask}
     * reverts the task so it is recollected on the next tick — a burst becomes backpressure instead
     * of HikariCP connection starvation.
     */
    @Bean
    public TaskExecutor draftCommitExecutor(@Autowired(required = false) TaskDecorator taskDecorator) {
        return buildSchedulerTaskExecutor("draftCommitExecutor-", taskDecorator);
    }

    @Bean
    public TaskExecutor draftCollectEraseScopeExecutor(@Autowired(required = false) TaskDecorator taskDecorator) {
        return buildSchedulerTaskExecutor("draftCollectEraseScopeExecutor-", taskDecorator);
    }

    @Bean
    public TaskExecutor twinChangeTaskExecutor(@Autowired(required = false) TaskDecorator taskDecorator) {
        return buildSchedulerTaskExecutor("twinChangeTaskExecutor-", taskDecorator);
    }

    @Bean
    public TaskExecutor historyNotificationTaskExecutor(@Autowired(required = false) TaskDecorator taskDecorator) {
        return buildSchedulerTaskExecutor("historyNotificationTaskExecutor-", taskDecorator);
    }

    @Bean
    public TaskExecutor twinTriggerTaskExecutor(@Autowired(required = false) TaskDecorator taskDecorator) {
        return buildSchedulerTaskExecutor("twinTriggerTaskExecutor-", taskDecorator);
    }

    @Bean(name = "attachmentDeleteTaskExecutor")
    public Executor attachmentDeleteTaskExecutor() {
        return buildBoundedVirtualThreadExecutor("attachmentDeleteTaskExecutor-");
    }

    @Bean(name = "logoutTaskExecutor")
    public Executor logoutTaskExecutor() {
        return buildBoundedVirtualThreadExecutor("logoutTaskExecutor-");
    }

    @Bean(name = "virtualThreadTaskScheduler")
    public TaskScheduler virtualThreadTaskScheduler(List<Scheduler> schedulerList) {
        var taskScheduler = new SimpleAsyncTaskScheduler();

        taskScheduler.setVirtualThreads(true);
        // Trigger threads hold DB connections while collecting/saving, so the limit follows the DB
        // pool, not the scheduler count. Auto = enough for all schedulers in parallel, capped at
        // dbPoolSize/5; override with twins.scheduler.trigger-concurrency (>= 0) if needed.
        int triggerConcurrency = schedulerTriggerConcurrency >= 0
                ? schedulerTriggerConcurrency
                : Math.min(Math.max(schedulerList.size(), 2), Math.max(2, dbPoolSize / 5));
        taskScheduler.setConcurrencyLimit(triggerConcurrency);
        taskScheduler.setThreadNamePrefix("task-scheduler-vt-");

        return taskScheduler;
    }

    @Bean(name = "emailTaskExecutor")
    public Executor taskExecutor() {
        return Executors.newFixedThreadPool(10);
    }

    private ThreadPoolTaskExecutor buildSchedulerTaskExecutor(String threadNamePrefix, TaskDecorator taskDecorator) {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(schedulerTaskCorePoolSize);
        executor.setMaxPoolSize(schedulerTaskMaxPoolSize);
        executor.setQueueCapacity(schedulerTaskQueueCapacity);
        // Reject once threads + queue are saturated (do not grow an unbounded queue).
        // SchedulerTaskRunner catches RejectedExecutionException and reverts the task for recollection.
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.AbortPolicy());
        executor.setThreadNamePrefix(threadNamePrefix);
        if (taskDecorator != null) executor.setTaskDecorator(taskDecorator);
        executor.initialize();
        return executor;
    }

    private Executor buildBoundedVirtualThreadExecutor(String threadNamePrefix) {
        // Keep virtual threads for IO-bound work, but cap concurrency so a burst cannot exhaust the
        // DB pool. At the limit the caller is throttled (blocked) until a slot frees.
        SimpleAsyncTaskExecutor executor = new SimpleAsyncTaskExecutor(threadNamePrefix);
        executor.setVirtualThreads(true);
        executor.setConcurrencyLimit(virtualExecutorConcurrency);
        return executor;
    }

//    @Bean(name = "cacheManagerRequestScope")
//    @RequestScope(proxyMode = ScopedProxyMode.TARGET_CLASS)
//    public CacheManager getCacheManager(){
//        return new ConcurrentMapCacheManager();
//    }
}