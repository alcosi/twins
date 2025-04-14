package org.twins.core.featurer.transition.trigger.messaging.rabbitmq.payloads;

import java.util.Set;
import java.util.UUID;

public record RabbitMqMessagePayloadFields(
    String requestId,
    UUID twinsId,
    UUID userId,
    UUID businessAccountId,
    UUID domainId,
    String operation,
    Set<UUID> fields
) {}
