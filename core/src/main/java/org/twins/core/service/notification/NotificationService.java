package org.twins.core.service.notification;

import alcosi.notification_manager.v1.Receiver;
import alcosi.notification_manager.v1.ReceiverServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.FeaturerService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.twins.core.dao.history.HistoryEntity;
import org.twins.core.dao.notification.HistoryNotificationContextCollectorEntity;
import org.twins.core.dao.notification.HistoryNotificationContextEntity;
import org.twins.core.dao.notification.HistoryNotificationRecipientEntity;
import org.twins.core.dao.notification.HistoryNotificationSchemaMapEntity;
import org.twins.core.featurer.notificator.context.ContextCollector;
import org.twins.core.featurer.notificator.recipient.RecipientResolver;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@Lazy
@RequiredArgsConstructor
public class NotificationService {

    private final HistoryNotificationSchemaMapService notificationSchemaMapService;
    private final FeaturerService featurerService;
    private ReceiverServiceGrpc.ReceiverServiceFutureStub receiverServiceFutureStub;

    @PostConstruct
    private void initReceiverServiceFutureStub() {
        ManagedChannel channel = ManagedChannelBuilder
                .forTarget("192.168.7.214:20108")
                .usePlaintext()
                .build();

        receiverServiceFutureStub = ReceiverServiceGrpc.newFutureStub(channel);
    }

    //todo on scheduler
//    @Scheduled(fixedRate = 5000)
    @Transactional
    public void collect() throws ServiceException {
        List<HistoryEntity> historyList = new ArrayList<>(); //todo get histories in statuses NOT SEND
        List<Receiver.SendNotificationCommand> notificationCommandList = new ArrayList<>();

        UUID notificationSchemaId = UUID.fromString("167261cd-ca6f-45dc-a7d5-cf4d2581b652");
        Set<HistoryNotificationSchemaMapEntity> schemaMaps = notificationSchemaMapService.getByNotificationSchemaAndEventCodes(notificationSchemaId, List.of("PROJECT_ADD", "TASK_ADD_COMMENT"));

        for (HistoryEntity history : historyList) {
            //todo нужно как-то определять, какою schemeNotificationMap вызввать для history
            HistoryNotificationSchemaMapEntity historyNotificationSchemaMapEntity = schemaMaps.stream().findFirst().get();

            Set<UUID> userIds = recipientResolve(historyNotificationSchemaMapEntity.getHistoryNotificationRecipient(), history);
            if (userIds.isEmpty())
                continue;
            String stringBusinessAccountId = history.getTwin().getOwnerBusinessAccountId().toString();
            String eventCode = historyNotificationSchemaMapEntity.getNotificationChannelEvent().getEventCode();
            Map<String, String> baseMap = fillBaseParams(stringBusinessAccountId, eventCode);
            Map<String, String> contextMap = collectHistoryContext(
                    historyNotificationSchemaMapEntity.getNotificationChannelEvent().getHistoryNotificationContext(),
                    baseMap,
                    history
            );

            //collect notifications
            notificationCommandList.add(Receiver.SendNotificationCommand.newBuilder()
                    .addAllUsersIds(userIds.stream().map(UUID::toString).collect(Collectors.toList()))
                    .putFilters("COMPANY_ID", stringBusinessAccountId)
                    .setEventId(eventCode)
                    .putAllData(contextMap)
                    .build()
            );

//            history.setStatus("SEND");
        }
//            historyService.saveSafe(history);

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

    private Map<String, String> fillBaseParams(String stringBusinessAccountId, String eventCode) {
        return new HashMap<>(Map.of(
                "COMPANY_ID", stringBusinessAccountId,
                "EVENT_ID", eventCode
        ));
    }
}
