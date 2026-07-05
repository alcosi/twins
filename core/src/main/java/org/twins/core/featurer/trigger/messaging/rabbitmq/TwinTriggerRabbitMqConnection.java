package org.twins.core.featurer.trigger.messaging.rabbitmq;

import lombok.extern.slf4j.Slf4j;
import org.cache2k.Cache;
import org.cache2k.Cache2kBuilder;
import org.cache2k.CacheEntry;
import org.cache2k.event.CacheEntryEvictedListener;
import org.cache2k.event.CacheEntryExpiredListener;
import org.cache2k.event.CacheEntryRemovedListener;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamString;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinStatusEntity;
import org.twins.core.featurer.trigger.TwinTrigger;

import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
public abstract class TwinTriggerRabbitMqConnection extends TwinTrigger {

    @FeaturerParam(name = "url", description = "rabbit server url")
    public static final FeaturerParamString url = new FeaturerParamString("url");

    protected static final Cache<String, CachingConnectionFactory> rabbitConnectionCache = new Cache2kBuilder<String, CachingConnectionFactory>() {
    }
            .name("TwinsConnectionCache")
            .entryCapacity(20)
            .expireAfterWrite(1, TimeUnit.HOURS)
            .timerLag(1, TimeUnit.SECONDS)
            // A factory that left the cache still holds its RabbitMQ connection + channels + heartbeat threads open;
            // GC won't close them, so destroy() it explicitly to avoid leaking connections on eviction/expiry/removal.
            // todo refactor connections mechanism
            .addAsyncListener((CacheEntryEvictedListener<String, CachingConnectionFactory>) (cache, entry) -> closeFactory(entry))
            .addAsyncListener((CacheEntryExpiredListener<String, CachingConnectionFactory>) (cache, entry) -> closeFactory(entry))
            .addAsyncListener((CacheEntryRemovedListener<String, CachingConnectionFactory>) (cache, entry) -> closeFactory(entry))
            .build();

    private static void closeFactory(CacheEntry<String, CachingConnectionFactory> entry) {
        CachingConnectionFactory factory = entry.getValue();
        if (factory != null) {
            log.info("Closing rabbit connectionFactory for URL: {}", entry.getKey());
            factory.destroy();
        }
    }

    @Override
    public void run(Properties properties, TwinEntity twinEntity, TwinStatusEntity srcTwinStatus, TwinStatusEntity dstTwinStatus, UUID jobTwinId) throws ServiceException {
        connect(properties);
        send(properties, twinEntity, srcTwinStatus, dstTwinStatus, jobTwinId);
    }

    public void connect(Properties properties) {
        String connectionUrl = url.extract(properties);
        rabbitConnectionCache.computeIfAbsent(connectionUrl, key -> {
            log.info("Creat new connectionFactory URL: {}", key);
            CachingConnectionFactory connectionFactory = new CachingConnectionFactory();
            connectionFactory.setUri(connectionUrl);
            connectionFactory.setCacheMode(CachingConnectionFactory.CacheMode.CHANNEL);
            connectionFactory.setChannelCacheSize(25);
            connectionFactory.setConnectionCacheSize(5);
            return connectionFactory;
        });
    }

    public abstract void send(Properties properties, TwinEntity twinEntity, TwinStatusEntity srcTwinStatus, TwinStatusEntity dstTwinStatus, UUID jobTwinId) throws ServiceException;

}
