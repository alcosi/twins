package org.twins.core.featurer.transition.rabbit;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.FeaturerType;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.featurer.FeaturerTwins;

import java.util.HashMap;
import java.util.Properties;

/**
 * RabbitTrigger is an abstract class responsible for handling interactions with RabbitMQ.
 * It enables the activation and execution of logic in secondary services by providing a
 * framework for establishing connections and sending messages via RabbitMQ.
 * This class must be extended to implement RabbitMQ-specific connection and message
 * sending logic. Subclasses are required to implement the abstract methods `connect` and `send`.
 * Usage Steps:
 * 1. Extracts properties for configuration from the provided trigger parameters.
 * 2. Establishes a connection to RabbitMQ using the extracted properties.
 * 3. Sends messages or triggers execution logic using the provided TwinEntity context.
 * Methods:
 * - connect(Properties): Abstract method to be implemented for establishing a connection to RabbitMQ.
 * - Send(Properties, TwinEntity): Abstract method to be implemented for sending messages or
 *   performing specific RabbitMQ operations.
 * Logging:
 * Logs relevant runtime details such as the name of the trigger being executed and the configured properties.
 * Exceptions:
 *  Throw `ServiceException` in case of any issues during the execution of the trigger or failures in RabbitMQ interactions.
 */
@FeaturerType(id = FeaturerTwins.TYPE_31,
        name = "RabbitTrigger",
        description = "Trigger for interacting with RabbitMQ and activating the execution of logic in secondary services.")
@Slf4j
public abstract class RabbitTrigger extends FeaturerTwins  {

    /**
     * Executes the trigger by extracting properties, establishing a connection, and performing
     * an action using the provided entity context.
     *
     * @param triggerParams A map containing input parameters required to configure the trigger. Keys and values are strings.
     * @param twinEntity The entity context used for processing or sending messages as part of the trigger execution.
     * @throws ServiceException If there is an issue with the execution of the trigger, including failures in connection
     *                          establishment or message sending operations.
     */
    public void run(HashMap<String, String> triggerParams, TwinEntity twinEntity) throws ServiceException {
        Properties properties = featurerService.extractProperties(this, triggerParams, new HashMap<>());
        log.info("Running trigger[{}] with params: {}", this.getClass().getSimpleName(), properties.toString());
        connect(properties);
        send(properties, twinEntity);
    }

    /**
     * Establishes a connection using the provided configuration properties.
     *
     * @param properties A Properties object containing configuration parameters for establishing the connection.
     *                   This may include details such as exchange names, queue names, and other RabbitMQ settings
     *                   required for creating the connection.
     */
    public abstract void connect(Properties properties) ;

    /**
     * Sends a message or performs an action related to a specific TwinEntity using
     * the provided configuration properties. This method should be implemented to define
     * the specific behavior for sending messages or interacting with RabbitMQ
     * within a given subclass.
     *
     * @param properties the configuration properties required for the message sending process,
     *                   including any connection or protocol-specific parameters.
     * @param twinEntity the context or entity associated with the message, used to define
     *                   the characteristics or data of the message being sent.
     */
    public abstract void send(Properties properties , TwinEntity twinEntity);
}
