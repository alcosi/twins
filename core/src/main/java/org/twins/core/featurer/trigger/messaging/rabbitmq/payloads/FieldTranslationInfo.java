package org.twins.core.featurer.trigger.messaging.rabbitmq.payloads;

import java.util.List;

public record FieldTranslationInfo(
    String fieldKey,
    String sourceLanguage,
    List<String> targetLanguages
) {}
