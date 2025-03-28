package org.twins.core.featurer.transition.trigger.messaging.rabbitmq;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.kit.Kit;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamString;
import org.cambium.featurer.params.FeaturerParamUUIDSet;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.twins.core.dao.i18n.I18nLocaleEntity;
import org.twins.core.dao.i18n.I18nLocaleRepository;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinStatusEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.domain.ApiUser;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.rabbit.AmpqManager;
import org.twins.core.service.twinclass.TwinClassFieldService;

import java.util.*;

@Service
@Slf4j
@Featurer(id = FeaturerTwins.ID_1505,
        name = "RabbitMqSendI18nFieldValueWithLocaleForOperation",
        description = "Trigger for sending translations event to rabbit")
@RequiredArgsConstructor
public class TransitionTriggerRabbitMqSendI18nFieldValueWithLocaleForOperation extends TransitionTriggerRabbitMqConnection {

    private final AmpqManager ampqManager;

    private final I18nLocaleRepository i18nLocaleRepository;

    @Autowired
    private TwinClassFieldService twinClassFieldService;

    private final AuthService authService;

    @FeaturerParam(name = "Exchange", description = "Name of exchange")
    public static final FeaturerParamString EXCHANGE = new FeaturerParamString("exchange");

    @FeaturerParam(name = "Queue", description = "Name of queue")
    public static final FeaturerParamString QUEUE = new FeaturerParamString("queue");

    @FeaturerParam(name = "Operation", description = "Name of operation")
    public static final FeaturerParamString OPERATION = new FeaturerParamString("operation");

    @FeaturerParam(name = "Fields", description = "Twin class field ids")
    public static final FeaturerParamUUIDSet FIELDS = new FeaturerParamUUIDSet("fields");

    @FeaturerParam(name = "Source locale", description = "Source language locale code")
    public static final FeaturerParamString SRC_LOCALE = new FeaturerParamString("src_locale");

    @Override
    public void send(Properties properties, TwinEntity twinEntity, TwinStatusEntity srcTwinStatus, TwinStatusEntity dstTwinStatus) throws ServiceException {
        ApiUser apiUser = authService.getApiUser();
        String sourceLanguage = SRC_LOCALE.extract(properties);

        List<I18nLocaleEntity> activeLocales = i18nLocaleRepository.findAllByActiveIsTrue();

        List<String> targetLanguages = activeLocales.stream().map(I18nLocaleEntity::getLocale).filter(locale -> !locale.equals(sourceLanguage)).toList();

        Kit<TwinClassFieldEntity, UUID> fieldEntities = twinClassFieldService.findEntitiesSafe(FIELDS.extract(properties));
        List<FieldTranslationInfo> fieldsToTranslate = new ArrayList<>();
        for (TwinClassFieldEntity field : fieldEntities)
            fieldsToTranslate.add(new FieldTranslationInfo(field.getKey(), sourceLanguage, new ArrayList<>(targetLanguages)));

        RabbitMqMessagePayload payload = new RabbitMqMessagePayload()
                .setTwinsId(twinEntity.getId())
                .setUserId(apiUser.getUserId())
                .setBusinessAccountId(apiUser.getBusinessAccountId())
                .setDomainId(apiUser.getDomainId())
                .setOperation(OPERATION.extract(properties))
                .setFields(fieldsToTranslate);

        ConnectionFactory factory = TransitionTriggerRabbitMqConnection.rabbitConnectionCache.get(
                TransitionTriggerRabbitMqConnection.URL.extract(properties));

        ampqManager.sendMessage(factory, EXCHANGE.extract(properties), QUEUE.extract(properties), payload);
    }

    @Data
    @Accessors(chain = true)
    private static class RabbitMqMessagePayload {
        private String requestId;
        private UUID twinsId;
        private UUID userId;
        private UUID businessAccountId;
        private UUID domainId;
        private String operation;
        private List<FieldTranslationInfo> fields;
    }

    @Data
    @AllArgsConstructor
    private static class FieldTranslationInfo {
        private String fieldKey;
        private String sourceLanguage;
        private List<String> targetLanguages;
    }
}

