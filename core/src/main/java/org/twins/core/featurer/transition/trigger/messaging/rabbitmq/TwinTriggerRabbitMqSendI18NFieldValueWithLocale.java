package org.twins.core.featurer.transition.trigger.messaging.rabbitmq;

import lombok.RequiredArgsConstructor;
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
import org.twins.core.featurer.transition.trigger.messaging.rabbitmq.payloads.FieldTranslationInfo;
import org.twins.core.featurer.transition.trigger.messaging.rabbitmq.payloads.RabbitMqMessagePayloadTranslation;
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
public class TwinTriggerRabbitMqSendI18NFieldValueWithLocale extends TwinTriggerRabbitMqConnection {

    private final AmpqManager ampqManager;

    private final I18nLocaleRepository i18nLocaleRepository;

    @Autowired
    private TwinClassFieldService twinClassFieldService;

    private final AuthService authService;

    @FeaturerParam(name = "Exchange", description = "Name of exchange")
    public static final FeaturerParamString exchange = new FeaturerParamString("exchange");

    @FeaturerParam(name = "Queue", description = "Name of queue")
    public static final FeaturerParamString queue = new FeaturerParamString("queue");

    @FeaturerParam(name = "Operation", description = "Name of operation")
    public static final FeaturerParamString operation = new FeaturerParamString("operation");

    @FeaturerParam(name = "Fields", description = "Twin class field ids")
    public static final FeaturerParamUUIDSet fields = new FeaturerParamUUIDSet("fields");

    @FeaturerParam(name = "Source locale", description = "Source language locale code")
    public static final FeaturerParamString src_locale = new FeaturerParamString("src_locale");

    @Override
    public void send(Properties properties, TwinEntity twinEntity, TwinStatusEntity srcTwinStatus, TwinStatusEntity dstTwinStatus) throws ServiceException {
        ApiUser apiUser = authService.getApiUser();
        String sourceLanguage = src_locale.extract(properties);

        List<I18nLocaleEntity> activeLocales = i18nLocaleRepository.findAllByActiveIsTrue();

        List<String> targetLanguages = activeLocales.stream().map(I18nLocaleEntity::getLocale).filter(locale -> !locale.equals(sourceLanguage)).toList();

        Kit<TwinClassFieldEntity, UUID> fieldEntities = twinClassFieldService.findEntitiesSafe(fields.extract(properties));
        List<FieldTranslationInfo> fieldsToTranslate = new ArrayList<>();
        for (TwinClassFieldEntity field : fieldEntities)
            fieldsToTranslate.add(new FieldTranslationInfo(field.getKey(), sourceLanguage, new ArrayList<>(targetLanguages)));

        RabbitMqMessagePayloadTranslation payload = new RabbitMqMessagePayloadTranslation(
                null,
                twinEntity.getId(),
                apiUser.getUserId(),
                apiUser.getBusinessAccountId(),
                apiUser.getDomainId(),
                operation.extract(properties),
                fieldsToTranslate
        );

        ConnectionFactory factory = TwinTriggerRabbitMqConnection.rabbitConnectionCache.get(
                TwinTriggerRabbitMqConnection.url.extract(properties));

        ampqManager.sendMessage(factory, exchange.extract(properties), queue.extract(properties), payload);
    }
}

