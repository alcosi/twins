package org.twins.core.featurer.transition.trigger.messaging.rabbitmq;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.util.CollectionUtils;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamBoolean;
import org.cambium.featurer.params.FeaturerParamString;
import org.cambium.featurer.params.FeaturerParamUUIDSet;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.stereotype.Service;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinStatusEntity;
import org.twins.core.domain.ApiUser;
import org.twins.core.domain.search.BasicSearch;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.params.FeaturerParamUUIDSetTwinsClassId;
import org.twins.core.featurer.params.FeaturerParamUUIDSetTwinsStatusId;
import org.twins.core.featurer.transition.trigger.messaging.rabbitmq.payloads.RabbitMqMessagePayloadTwin;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.rabbit.AmpqManager;
import org.twins.core.service.twin.TwinSearchService;

import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;

@Service
@Slf4j
@Featurer(id = FeaturerTwins.ID_1507,
        name = "RabbitMqSendTwin",
        description = "Trigger for sending event to rabbit")
@RequiredArgsConstructor
public class TwinTriggerRabbitMqSendTwinChildrenInStatuses extends TwinTriggerRabbitMqConnection {

    private final AmpqManager ampqManager;

    private final AuthService authService;

    private final TwinSearchService  twinSearchService;

    @FeaturerParam(name = "ChildrenTwinStatusIdList", description = "Twin.Status.IDs of child twin")
    public static final FeaturerParamUUIDSet childrenTwinStatusIdList = new FeaturerParamUUIDSetTwinsStatusId("childrenTwinStatusIdList");

    @FeaturerParam(name = "ChildrenTwinStatusIdList", description = "Twin.Class.IDs of child twin", optional = true)
    public static final FeaturerParamUUIDSet childrenTwinClassIdList = new FeaturerParamUUIDSetTwinsClassId("childrenTwinClassIdList");

    @FeaturerParam(name = "Exclude", description = "Exclude(true)/Include(false) child-field's Twin.Status.IDs from query result")
    public static final FeaturerParamBoolean exclude = new FeaturerParamBoolean("exclude");

    @FeaturerParam(name = "Exchange", description = "Name of exchange")
    public static final FeaturerParamString exchange = new FeaturerParamString("exchange");

    @FeaturerParam(name = "Queue", description = "Name of queue")
    public static final FeaturerParamString queue = new FeaturerParamString("queue");

    @FeaturerParam(name = "Operation", description = "Name of operation")
    public static final FeaturerParamString operation = new FeaturerParamString("operation");


    @Override
    public void send(Properties properties, TwinEntity twinEntity, TwinStatusEntity srcTwinStatus, TwinStatusEntity dstTwinStatus) throws ServiceException {
        ApiUser apiUser = authService.getApiUser();
        BasicSearch search = new BasicSearch();
        search
                .addHeadTwinId(twinEntity.getId())
                .addStatusId(childrenTwinStatusIdList.extract(properties), exclude.extract(properties));
        Set<UUID> twinClassIds = childrenTwinClassIdList.extract(properties);
        if (CollectionUtils.isNotEmpty(twinClassIds)) {
            search.addTwinClassExtendsHierarchyContainsId(twinClassIds);
        }
        List<TwinEntity> children = twinSearchService.findTwins(search);

        log.debug("Sending to Rabbit");
        ConnectionFactory factory = TwinTriggerRabbitMqConnection.rabbitConnectionCache.get(
                TwinTriggerRabbitMqConnection.url.extract(properties));

        RabbitMqMessagePayloadTwin payload;
        for (TwinEntity child : children) {
            payload = new RabbitMqMessagePayloadTwin(
                    child.getId(),
                    apiUser.getUserId(),
                    apiUser.getDomainId(),
                    apiUser.getBusinessAccountId(),
                    operation.extract(properties)
            );
            ampqManager.sendMessage(factory, exchange.extract(properties), queue.extract(properties), payload);
        }
        log.debug("Done sending to Rabbit");
    }
}
