package org.twins.core.featurer.trigger.messaging.rabbitmq.payloads;

import java.util.List;
import java.util.UUID;

public record RabbitMqMessagePayloadTranslation(
    String requestId,
    UUID twinsId,
    UUID userId,
    UUID businessAccountId,
    UUID domainId,
    String operation,
    List<FieldTranslationInfo> fields
) {}
