package org.twins.core.featurer.notificator.notifier;

import alcosi.notification_manager.v1.Receiver;
import alcosi.notification_manager.v1.ReceiverServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.springframework.stereotype.Component;
import org.twins.core.dao.history.HistoryEntity;
import org.twins.core.dao.notification.*;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.notificator.context.ContextCollector;
import org.twins.core.featurer.notificator.recipient.RecipientResolver;

import java.net.URI;
import java.util.*;
import java.util.stream.Collectors;

@Component
@Featurer(id = FeaturerTwins.ID_4801,
        name = "Notifier Alcosi Notification Manager",
        description = "")
public class NotifierAlcosiNotificationManager extends Notifier {

    @Override
    protected void notify(List<HistoryEntity> historyList, HistoryNotificationSchemaMapEntity schemaMap, Properties properties) throws ServiceException {
        String hostDomainBaseUriValue = getHostDomainBaseUri(properties);

        ReceiverServiceGrpc.ReceiverServiceFutureStub receiverServiceFutureStub = getOrCreateStub(hostDomainBaseUriValue);

        List<Receiver.SendNotificationCommand> notificationCommandList = new ArrayList<>();

        for (HistoryEntity history : historyList) {
            Set<UUID> userIds = recipientResolve(schemaMap.getHistoryNotificationRecipient(), history);
            if (userIds.isEmpty())
                continue;

            String stringBusinessAccountId = history.getTwin().getOwnerBusinessAccountId().toString();
            String eventCode = schemaMap.getNotificationChannelEvent().getEventCode();

            Set<String> requiredKeyParams = getRequiredKeyParams(properties);
            Map<String, String> baseMap = fillBaseParams(requiredKeyParams, stringBusinessAccountId, eventCode);

            Map<String, String> contextMap = collectHistoryContext(
                    schemaMap.getNotificationChannelEvent().getHistoryNotificationContext(),
                    baseMap,
                    history
            );

            notificationCommandList.add(Receiver.SendNotificationCommand.newBuilder()
                    .addAllUsersIds(userIds.stream().map(UUID::toString).collect(Collectors.toList()))
                    .putFilters("COMPANY_ID", stringBusinessAccountId)
                    .setEventId(eventCode)
                    .putAllData(contextMap)
                    .build()
            );
        }

        for (Receiver.SendNotificationCommand notificationCommand : notificationCommandList) {
            receiverServiceFutureStub.sendNotification(notificationCommand);
        }
    }

    private Set<UUID> recipientResolve(HistoryNotificationRecipientEntity notificationRecipient, HistoryEntity history) throws ServiceException {
        RecipientResolver recipientResolver = featurerService.getFeaturer(notificationRecipient.getRecipientResolverFeaturer(), RecipientResolver.class);
        return recipientResolver.resolve(history, notificationRecipient.getRecipientResolverParams());
    }

    private Map<String, String> collectHistoryContext(HistoryNotificationContextEntity historyNotificationContext, Map<String, String> contextMap, HistoryEntity history) throws ServiceException {
        for (HistoryNotificationContextCollectorEntity contextCollector : historyNotificationContext.getContextCollectors()) {
            ContextCollector collector = featurerService.getFeaturer(contextCollector.getContextCollectorFeaturer(), ContextCollector.class);
            contextMap = collector.collectData(history, contextMap, contextCollector.getContextCollectorParams());
        }
        return contextMap;
    }

    private Map<String, String> fillBaseParams(Set<String> requiredKeyParams, String... values) throws ServiceException {
        if (requiredKeyParams == null || requiredKeyParams.isEmpty()) {
            return new HashMap<>();
        }

        if (requiredKeyParams.size() != values.length) {
            throw new ServiceException(ErrorCodeTwins.NOTIFICATION_REQUIRED_PARAMS);
        }

        Iterator<String> valueIterator = Arrays.asList(values).iterator();

        return requiredKeyParams.stream()
                .collect(Collectors.toMap(
                        key -> key,
                        key -> valueIterator.next()
                ));
    }

    protected Set<String> getRequiredKeyParams(Properties properties) {
        List<String> list = requiredParamKeys.extract(properties);
        if (list == null || list.isEmpty()) {
            return Collections.emptySet();
        }
        return list.stream().map(String::toUpperCase).collect(Collectors.toSet());
    }
}
