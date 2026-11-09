package org.twins.core.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskDecorator;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.twins.core.holder.EntityRequestCacheHolder;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync
public class AsyncConfig {
    /**
     * {@code @Async} executor: fixed pool of 5 with an unbounded queue (same shape as the former
     * {@code Executors.newFixedThreadPool(5)}), plus the {@link EntityRequestCacheHolder} cleanup
     * decorator — pooled threads here must follow the same cleanup contract as web workers.
     */
    @Bean
    public Executor taskExecutor(TaskDecorator entityRequestCacheCleanupTaskDecorator) {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(5);
        executor.setThreadNamePrefix("async-");
        executor.setTaskDecorator(entityRequestCacheCleanupTaskDecorator);
        executor.initialize();
        return executor;
    }
}
