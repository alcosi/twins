package org.twins.core.service.rabbit;

import lombok.extern.slf4j.Slf4j;
import org.cache2k.Cache;
import org.cache2k.Cache2kBuilder;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

// todo change doc
/**
 * Centralized factory/cache for RabbitMQ {@link CachingConnectionFactory} instances.
 * <p>
 * Previously, each trigger managed its own static cache; this service consolidates that logic so that
 * any Spring-managed component can obtain (and reuse) a connection factory simply by URL.
 */
@Service
@Slf4j
public class RabbitService {

    private final Cache<String, CachingConnectionFactory> rabbitConnectionCache = new Cache2kBuilder<String, CachingConnectionFactory>() {
    }
            .name("TwinsConnectionCache")
            .entryCapacity(20)
            .expireAfterWrite(1, TimeUnit.HOURS)
            .timerLag(1, TimeUnit.SECONDS)
            .build();

    private final Cache<String, RabbitAdmin> rabbitAdminCache = new Cache2kBuilder<String, RabbitAdmin>() {
    }
            .name("TwinsRabbitAdminCache")
            .entryCapacity(20)
            .expireAfterWrite(1, TimeUnit.HOURS)
            .timerLag(1, TimeUnit.SECONDS)
            .build();

    private final Cache<String, Boolean> declarationCache = new Cache2kBuilder<String, Boolean>() {}
            .entryCapacity(20)
            .expireAfterWrite(1, TimeUnit.HOURS)
            .timerLag(1, TimeUnit.SECONDS)
            .build();


    public Boolean isDeclared(String url, String exchangeName, String queueName) {
        String cacheKey = url + "|" + exchangeName + "|" + queueName;

        return declarationCache.computeIfAbsent(cacheKey, key -> {
            try {
                RabbitAdmin rabbitAdmin = getRabbitAdmin(url);
                Exchange exchange = new DirectExchange(exchangeName, true, false);
                Queue queue = new Queue(queueName, true, false, false);
                Binding binding = BindingBuilder.bind(queue).to(exchange).with(queueName).noargs();

                rabbitAdmin.declareExchange(exchange);
                rabbitAdmin.declareQueue(queue);
                rabbitAdmin.declareBinding(binding);

                return true;
            } catch (Exception e) {
                log.error("Unable to declare rabbit objects", e);

                return false;
            }
        });
    }

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

    public RabbitAdmin getRabbitAdmin(String connectionUrl) {
        return rabbitAdminCache.computeIfAbsent(connectionUrl, url -> {
            log.info("Create new RabbitAdmin for URL: {}", url);

            return new RabbitAdmin(getConnectionFactory(connectionUrl));
        });
    }
}
