package org.twins.core.service.notification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.FeaturerService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.twins.core.dao.history.HistoryEntity;
import org.twins.core.dao.notification.*;
import org.twins.core.featurer.notificator.context.ContextCollector;
import org.twins.core.featurer.notificator.recipient.RecipientResolver;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@Lazy
@RequiredArgsConstructor
public class NotificationService {

    private final HistoryNotificationSchemaMapService notificationSchemaMapService;
    private final FeaturerService featurerService;

    // какой-то шедулер дергает метод по сбору данных
    public void collect() throws ServiceException {
        //todo status for history
        HistoryEntity history = new HistoryEntity();

        // changeAssignee history
        HistoryNotificationSchemaMapEntity schemaMap = notificationSchemaMapService.findEntitySafe(UUID.fromString("ee3c93c4-9143-4787-bfba-4d9fd7e77c8c"));

        //todo some logic for schema and channel
        NotificationSchemaEntity notificationSchema = schemaMap.getNotificationSchema();
        NotificationChannelEntity notificationChannel = schemaMap.getNotificationChannel();

        Set<UUID> userIds = recipientResolve(schemaMap.getHistoryNotificationRecipient(), history);
        Map<String, String> contextMap = collectHistoryContext(schemaMap.getHistoryNotificationContext(), history);

        return;
    }

    private Set<UUID> recipientResolve(HistoryNotificationRecipientEntity notificationRecipient, HistoryEntity history) throws ServiceException {
        RecipientResolver recipientResolver = featurerService.getFeaturer(notificationRecipient.getRecipientResolverFeaturer(), RecipientResolver.class);
        return recipientResolver.resolve(history, notificationRecipient.getRecipientResolverParams());
    }

    private Map<String, String> collectHistoryContext(HistoryNotificationContextEntity historyNotificationContext, HistoryEntity history) throws ServiceException {
        Map<String, String> context = new HashMap<>();
        for (HistoryNotificationContextCollectorEntity contextCollector : historyNotificationContext.getContextCollectors()) {
            ContextCollector collector = featurerService.getFeaturer(contextCollector.getContextCollectorFeaturer(), ContextCollector.class);
            context = collector.collect(history, context, contextCollector.getContextCollectorParams());
        }
        return context;
    }
}
