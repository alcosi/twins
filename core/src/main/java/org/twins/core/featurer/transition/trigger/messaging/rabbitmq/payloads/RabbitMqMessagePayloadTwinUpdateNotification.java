package org.twins.core.featurer.transition.trigger.messaging.rabbitmq.payloads;

import java.util.List;
import java.util.UUID;

public record RabbitMqMessagePayloadTwinUpdateNotification(
        UUID twinId,
        List<UUID> userIds
) {
}
