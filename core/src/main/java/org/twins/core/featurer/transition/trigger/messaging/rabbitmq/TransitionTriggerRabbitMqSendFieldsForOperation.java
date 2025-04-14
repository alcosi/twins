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
import org.twins.core.dao.i18n.I18nLocaleRepository;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinStatusEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.domain.ApiUser;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.transition.trigger.messaging.rabbitmq.payloads.FieldTranslationInfo;
import org.twins.core.featurer.transition.trigger.messaging.rabbitmq.payloads.RabbitMqMessagePayloadFields;
import org.twins.core.featurer.transition.trigger.messaging.rabbitmq.payloads.RabbitMqMessagePayloadTranslation;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.rabbit.AmpqManager;
import org.twins.core.service.twinclass.TwinClassFieldService;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

@Service
@Slf4j
@Featurer(id = FeaturerTwins.ID_1506,
        name = "RabbitMqSendFieldsForOperation",
        description = "Trigger for sending fields to rabbit")
@RequiredArgsConstructor
public class TransitionTriggerRabbitMqSendFieldsForOperation extends TransitionTriggerRabbitMqConnection {

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

    @Override
    public void send(Properties properties, TwinEntity twinEntity, TwinStatusEntity srcTwinStatus, TwinStatusEntity dstTwinStatus) throws ServiceException {
        ApiUser apiUser = authService.getApiUser();

        RabbitMqMessagePayloadFields payload = new RabbitMqMessagePayloadFields(
                null,
                twinEntity.getId(),
                apiUser.getUserId(),
                apiUser.getBusinessAccountId(),
                apiUser.getDomainId(),
                OPERATION.extract(properties),
                FIELDS.extract(properties)
        );

        ConnectionFactory factory = TransitionTriggerRabbitMqConnection.rabbitConnectionCache.get(
                TransitionTriggerRabbitMqConnection.URL.extract(properties));

        ampqManager.sendMessage(factory, EXCHANGE.extract(properties), QUEUE.extract(properties), payload);
    }
}

