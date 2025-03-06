package org.twins.core.featurer.transition.trigger.messaging.rabbitmq;

import lombok.extern.slf4j.Slf4j;
import org.cache2k.Cache;
import org.cache2k.Cache2kBuilder;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamString;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinStatusEntity;
import org.twins.core.featurer.transition.trigger.TransitionTrigger;

import java.util.Properties;
import java.util.concurrent.TimeUnit;

@Slf4j
public abstract class TransitionTriggerRabbitMqConnection extends TransitionTrigger {

    //todo like this amqp://user:pass@host:10000/vhost
    @FeaturerParam(name = "url", description = "rabbit server url", optional = false)
    public static final FeaturerParamString URL = new FeaturerParamString("url");

    protected static final Cache<String, CachingConnectionFactory> rabbitConnectionCache = new Cache2kBuilder<String, CachingConnectionFactory>() {
    }
            .name("TwinsConnectionCache")
            .entryCapacity(20)
            .expireAfterWrite(1, TimeUnit.HOURS)
            .timerLag(1, TimeUnit.SECONDS)
            .build();

    @Override
    public void run(Properties properties, TwinEntity twinEntity, TwinStatusEntity srcTwinStatus, TwinStatusEntity dstTwinStatus) throws ServiceException {
        connect(properties);

//        // todo we can skip this step if we don't want create queue and sure what rabbit has configured
//        createQueue(properties,);

        send(properties, twinEntity, srcTwinStatus, dstTwinStatus);
    }


    public void connect(Properties properties) {
        String connectionUrl = URL.extract(properties);
        rabbitConnectionCache.computeIfAbsent(connectionUrl, key -> {
            log.info("Creat new connectionFactory URL: {}", key);
            CachingConnectionFactory connectionFactory; connectionFactory = new CachingConnectionFactory();
            connectionFactory.setUri(connectionUrl);
            connectionFactory.setCacheMode(CachingConnectionFactory.CacheMode.CHANNEL);
            connectionFactory.setChannelCacheSize(25);
            connectionFactory.setConnectionCacheSize(5);
            rabbitConnectionCache.put(connectionUrl, connectionFactory);
            return connectionFactory;
        });
    }

//    private void createQueue(Properties properties, String mainQueueName, String retryQueueName, String dlxName, String mainExc, Integer ttl)  {
//        RabbitAdmin amqpAdmin = new RabbitAdmin (rabbitConnectionCache.get(url.extract(properties)));
//        DirectExchange mainExchange = new DirectExchange(mainExc, true, false);
//        amqpAdmin.declareExchange(mainExchange);
//
//        Map<String, Object> mainQueueArgs = new HashMap<>();
//        mainQueueArgs.put("x-dead-letter-exchange", dlxName);
//        mainQueueArgs.put("x-dead-letter-routing-key", retryQueueName);
//        Queue mainQueue = new Queue(mainQueueName, true, false, false, mainQueueArgs);
//        amqpAdmin.declareQueue(mainQueue);
//        log.debug("Create connection to queue {}: {}", mainExchange, mainQueueName);
//
//        if (StringUtils.isNotBlank(retryQueueName) && StringUtils.isNotBlank(dlxName)) {
//
//            DirectExchange dlxExchange = new DirectExchange(dlxName, true, false);
//            amqpAdmin.declareExchange(dlxExchange);
//
//            Map<String, Object> retryQueueArgs = new HashMap<>();
//            retryQueueArgs.put("x-message-ttl", ttl == null ? 10000 : ttl);
//            retryQueueArgs.put("x-dead-letter-exchange", dlxName);
//            retryQueueArgs.put("x-dead-letter-routing-key", mainQueueName);
//            Queue retryQueue = new Queue(retryQueueName, true, false, false, retryQueueArgs);
//            amqpAdmin.declareQueue(retryQueue);
//            logCreating(dlxName, retryQueueName);
//
//            Binding dlxBinding = BindingBuilder.bind(retryQueue)
//                    .to(dlxExchange)
//                    .with(retryQueueName);
//            amqpAdmin.declareBinding(dlxBinding);
//            logCreating(dlxExchange.getName(), retryQueueName);
//
//            Binding dlxMainBinding = BindingBuilder.bind(mainQueue)
//                    .to(dlxExchange)
//                    .with(mainQueueName);
//            amqpAdmin.declareBinding(dlxMainBinding);
//        }
//
//        Binding mainBinding = BindingBuilder.bind(mainQueue)
//                .to(mainExchange)
//                .with(mainQueueName);
//        amqpAdmin.declareBinding(mainBinding);
//        logCreating(mainExchange.getName(), mainQueueName);
//    }
//
//    private void logCreating(String exchange, String queue) {
//        log.debug("Create  binding for Exchange {} and Queue {}", exchange, queue);
//    }

    public abstract void send(Properties properties, TwinEntity twinEntity, TwinStatusEntity srcTwinStatus, TwinStatusEntity dstTwinStatus);

}
