package org.twins.core.featurer.trigger.messaging.rabbitmq.payloads;

import java.util.UUID;

public record RabbitMqMessagePayloadTwin(
    UUID twinsId,
    UUID userId,
    UUID domainId,
    UUID businessAccountId,
    String operation
) {}
