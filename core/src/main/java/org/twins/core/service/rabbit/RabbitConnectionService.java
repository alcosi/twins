package org.twins.core.service.rabbit;

import lombok.extern.slf4j.Slf4j;
import org.cache2k.Cache;
import org.cache2k.Cache2kBuilder;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * Centralized factory/cache for RabbitMQ {@link CachingConnectionFactory} instances.
 * <p>
 * Previously, each trigger managed its own static cache; this service consolidates that logic so that
 * any Spring-managed component can obtain (and reuse) a connection factory simply by URL.
 */
@Service
@Slf4j
public class RabbitConnectionService {

    private static final Cache<String, CachingConnectionFactory> rabbitConnectionCache = new Cache2kBuilder<String, CachingConnectionFactory>() {
    }
            .name("TwinsConnectionCache")
            .entryCapacity(20)
            .expireAfterWrite(1, TimeUnit.HOURS)
            .timerLag(1, TimeUnit.SECONDS)
            .build();

    /**
     * Get cached {@link CachingConnectionFactory} for the url, or create one if absent.
     */
    public CachingConnectionFactory getConnectionFactory(String connectionUrl) {
        return rabbitConnectionCache.computeIfAbsent(connectionUrl, key -> {
            log.info("Create new CachingConnectionFactory for URL: {}", key);
            CachingConnectionFactory connectionFactory = new CachingConnectionFactory();
            connectionFactory.setUri(connectionUrl);
            connectionFactory.setCacheMode(CachingConnectionFactory.CacheMode.CHANNEL);
            connectionFactory.setChannelCacheSize(25);
            connectionFactory.setConnectionCacheSize(5);
            // Enable publisher confirms and returns
            connectionFactory.setPublisherConfirmType(CachingConnectionFactory.ConfirmType.CORRELATED);
            connectionFactory.setPublisherReturns(true);
            return connectionFactory;
        });
    }
}
