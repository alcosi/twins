package org.twins.core.featurer.transition.rabbit;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamInt;
import org.cambium.featurer.params.FeaturerParamString;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.service.rabbit.DynamicAmpqManager;

import java.util.Properties;

/**
 * RabbitConnectTrigger is a service class designed to manage RabbitMQ operations, including
 * establishing connections and sending messages. It extends RabbitTrigger and integrates with
 * the Featurer framework for parameter configuration and dynamic RabbitMQ setup.
 * This class supports dynamic AMQP (Advanced Message Queuing Protocol) operations, enabling
 * the configuration and interaction with RabbitMQ exchanges, queues, and retry mechanisms.
 * It is primarily responsible for handling messaging workflows, defining exchanges and queues,
 * and supporting retry and fault handling in RabbitMQ.
 * Key features of RabbitConnectTrigger include:
 * - Dynamic RabbitMQ connection creation for main, retry, and dead-letter exchange/queues.
 * - Message delivery to the configured main exchange and queue.
 * - Support for retry and dead-letter mechanisms in the messaging workflow.
 */
@Service
@Slf4j
@Featurer(id = FeaturerTwins.ID_3101,
        name = "ConnectEventTrigger",
        description = "Trigger for secondary services on execution of connection logic")
@RequiredArgsConstructor
public class RabbitConnectTrigger extends RabbitTrigger {

    /**
     * Manages dynamic AMQP (Advanced Message Queuing Protocol) operations, including connection creation and message
     * delivery to RabbitMQ components such as exchanges and queues.
     * dynamicAmpqManager is a lazily initialized instance, ensuring AMQP resources are only initialized when required.
     * It facilitates creating connections between the main exchange, retry, and dead-letter queues with configurable parameters,
     * and sends messages to the specified exchange and queue.
     * This variable is integral to managing messaging workflows, supporting retry mechanisms and fault handling in RabbitMQ.
     */
    @Lazy
    private final DynamicAmpqManager dynamicAmpqManager;

    /**
     * Represents the primary exchange in the RabbitMQ configuration.
     * This parameter is used to define the name of the main exchange where messages
     * are routed during RabbitMQ operations. It is a static feature parameter of
     * type {@code FeaturerParamString} with a default key of "mainExc".
     * The {@code MAIN_EXCHANGE} is annotated with {@code @FeaturerParam} to specify
     * its metadata for feature registration:
     * This parameter is used within methods that establish RabbitMQ connections
     * and send messages, among other operations.
     */
    @FeaturerParam(name = "Main exchange", description = "Name of main exchange", order = 1)
    private static final FeaturerParamString MAIN_EXCHANGE =  new FeaturerParamString("mainExc");
    /**
     * Represents the configuration parameter for the main queue used in RabbitMQ communication.
     * This field is annotated with {@code @FeaturerParam} to specify metadata for the feature configuration.
     * The parameter's name is "Main queue", and its description indicates it contains the name of the main queue.
     * It is included with an order value of 2 for prioritization in feature-related operations.
     */
    @FeaturerParam(name = "Main queue", description = "Name of main queue", order = 2)
    private static final FeaturerParamString MAIN_QUEUE = new FeaturerParamString("mainQueue");
    /**
     * Represents the name of the dead letter exchange in a RabbitMQ setup.
     * It is used as a configuration parameter to define the exchange where
     * messages that cannot be routed or processed should be forwarded to.
     * This parameter is configured using the Featurer framework and is marked
     * with an order of 3 in the parameter list.
     */
    @FeaturerParam(name = "Dead letter exchange", description = "Name of dead letter exchange", order = 3)
    private static final FeaturerParamString DEAD_EXCHANGE = new FeaturerParamString("dlxName");
    /**
     * Represents the configuration parameter for the name of the retry queue.
     * This parameter is annotated with {@link FeaturerParam} to specify its
     * metadata, like its name, description, and order of inclusion, which is used
     * within the context of a Featurer for dynamic RabbitMQ connection management.
     * This parameter is used during the establishment of connections and
     * processing of retry logic for queued messages.
     */
    @FeaturerParam(name = "Retry queue", description = "Name of retry queue", order = 4)
    private static final FeaturerParamString RETRY_QUEUE = new FeaturerParamString("retryQueueName");
    /**
     * Configuration parameter representing the delay time in milliseconds
     * between retry attempts. This parameter is used to specify the time interval
     * to wait before attempting a retry operation.
     * Annotated with {@link FeaturerParam} to define metadata for the parameter
     */
    @FeaturerParam(name = "Retry time delay", description = "Delay in ms between retry", order = 5)
    private static final FeaturerParamInt WAIT_TIME = new FeaturerParamInt("ttl");

    /**
     * Establishes a connection to the RabbitMQ broker based on the provided properties.
     * Configures the necessary exchanges, queues, and retry mechanisms for RabbitMQ.
     *
     * @param properties Configuration properties used to set up the connection.
     *                    Expected keys in the properties:
     *                    - MAIN_EXCHANGE: Name of the main RabbitMQ exchange.
     *                    - MAIN_QUEUE: Name of the main RabbitMQ queue.
     *                    - DEAD_EXCHANGE: Name of the dead letter exchange.
     *                    - RETRY_QUEUE: Name of the retry queue.
     *                    - WAIT_TIME: Retry delay in milliseconds.
     */
    @Override
    public void connect(Properties properties) {
        log.debug("Connecting to Rabbit");
        dynamicAmpqManager.createConnection(MAIN_EXCHANGE.extract(properties),RETRY_QUEUE.extract(properties),
                DEAD_EXCHANGE.extract(properties),MAIN_QUEUE.extract(properties),WAIT_TIME.extract(properties) );
    }

    /**
     * Sends a message containing the twin entity's external ID to a RabbitMQ queue using the provided properties.
     *
     * @param properties A Properties object containing configuration parameters for the RabbitMQ exchange and queue.
     * @param twinEntity The TwinEntity object containing the external ID to be sent as a message.
     */
    @Override
    public void send(Properties properties, TwinEntity twinEntity) {
        log.debug("Sending to Rabbit");
       dynamicAmpqManager.sendMessage(MAIN_EXCHANGE.extract(properties), MAIN_QUEUE.extract(properties), twinEntity.getExternalId());
    }
}
