package org.twins.core.config;

import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for setting up RabbitMQ integration within the application.
 * This class provides configurations needed to establish a connection with
 * a RabbitMQ server, including settings for the host, port, username, and password.
 * It defines a bean for {@link CachingConnectionFactory} to manage connections efficiently.
 * The configuration values for RabbitMQ are externalized and injected
 * using Spring's @Value annotation, which references properties defined
 * in the application's configuration file or environment variables.
 * The configured {@link CachingConnectionFactory} bean can be autowired
 * where necessary to interact with the RabbitMQ server.
 */
@Configuration
public class RabbitMQConfig {

    /**
     * The hostname or IP address of the RabbitMQ broker.
     * This property is injected from the application's external configuration
     * using the key `spring.rabbitmq.host`.
     * It specifies the address of the RabbitMQ instance that will be used
     * for creating connections in the application.
     */
    @Value("${spring.rabbitmq.host}")
    private String host;

    /**
     * Represents the port on which the RabbitMQ service is running.
     * This value is automatically injected from the application properties
     * using the "spring.rabbitmq.port" configuration key.
     */
    @Value("${spring.rabbitmq.port}")
    private int port;

    /**
     * Represents the username used for authenticating with the RabbitMQ server.
     * The value is injected from the Spring environment properties using the
     * key "spring.rabbitmq.username".
     */
    @Value("${spring.rabbitmq.username}")
    private String username;

    /**
     * Configuration property that holds the password used for connecting to the RabbitMQ server.
     * The value is injected from the application's configuration file (e.g., application.properties or environment variables)
     * and corresponds to the property `spring.rabbitmq.password`.
     * This password is used by the RabbitMQ connection factory to authenticate the client with the RabbitMQ server.
     * It is recommended to secure this property and avoid hardcoding sensitive credentials.
     */
    @Value("${spring.rabbitmq.password}")
    private String password;

    /**
     * Configures and provides a {@link CachingConnectionFactory} bean for managing RabbitMQ connections.
     * The connection factory is initialized with the provided host, port, username, and password.
     *
     * @return a configured {@link CachingConnectionFactory} instance for RabbitMQ connections
     */
    @Bean
    public CachingConnectionFactory connectionFactory() {
        CachingConnectionFactory connectionFactory = new CachingConnectionFactory();
        connectionFactory.setHost(host);
        connectionFactory.setPort(port);
        connectionFactory.setUsername(username);
        connectionFactory.setPassword(password);
        return connectionFactory;
    }
}
