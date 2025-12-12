package org.twins.core.service.notification;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.kit.KitGroupedObj;
import org.cambium.common.util.LoggerUtils;
import org.cambium.featurer.FeaturerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.twins.core.dao.history.HistoryEntity;
import org.twins.core.dao.notification.*;
import org.twins.core.enums.HistoryNotificationTaskStatus;
import org.twins.core.featurer.notificator.context.ContextCollector;
import org.twins.core.featurer.notificator.notifier.Notifier;
import org.twins.core.featurer.notificator.recipient.RecipientResolver;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.*;

@Component
@Scope("prototype")
@Slf4j
public class HistoryNotificationTask implements Runnable {
    private final HistoryNotificationTaskEntity historyNotificationEntity;
    @Autowired
    private HistoryNotificationSchemaMapRepository historyNotificationSchemaMapEntityRepository;
    @Autowired
    private HistoryNotificationTaskRepository historyNotificationTaskRepository;
    @Autowired
    private FeaturerService featurerService;
    @Autowired
    private NotificationContextService notificationContextService;
    
    private final Map<UUID, Map<String, String>> contextCache = new HashMap<>();

    public HistoryNotificationTask(HistoryNotificationTaskEntity historyNotificationEntity) {
        this.historyNotificationEntity = historyNotificationEntity;
    }

    @Override
    public void run() {
        try {
            HistoryEntity history = historyNotificationEntity.getHistory();
            LoggerUtils.logSession(history.getHistoryBatchId());
            LoggerUtils.logController("historyNotificationTask");
            LoggerUtils.logPrefix(STR."HISTORY[\{historyNotificationEntity.getId()}]:");
            log.info("Performing history notification task: {}", historyNotificationEntity.logDetailed());
            List<HistoryNotificationSchemaMapEntity> configs = historyNotificationSchemaMapEntityRepository.findByHistoryTypeIdAndTwinClassIdAndNotificationSchemaId(
                    history.getHistoryType().name(),
                    history.getTwin().getTwinClassId(),
                    historyNotificationEntity.getNotificationSchemaId()
            );
            KitGroupedObj<HistoryNotificationSchemaMapEntity, UUID, UUID, NotificationChannelEventEntity> notificationConfigsGroupedByChannelEvent = new KitGroupedObj<>(
                    configs,
                    HistoryNotificationSchemaMapEntity::getId,
                    HistoryNotificationSchemaMapEntity::getNotificationChannelEventId,
                    HistoryNotificationSchemaMapEntity::getNotificationChannelEvent);
            
            int recipientsCount = 0;
            for (var entry : notificationConfigsGroupedByChannelEvent.getGroupedMap().entrySet()) {
                var recipientIds = new HashSet<UUID>();
                for (var config : entry.getValue()) {
                    recipientIds.addAll(recipientResolve(config.getHistoryNotificationRecipient(), history));
                }
                if (recipientIds.isEmpty())
                    continue;
                recipientsCount += recipientIds.size();
                var channelEvent = notificationConfigsGroupedByChannelEvent.getGroupingObject(entry.getKey());
                var context = getContext(channelEvent.getNotificationContextId(), history);
                NotificationChannelEntity notificationChannel = channelEvent.getNotificationChannel();
                Notifier notifier = featurerService.getFeaturer(notificationChannel.getNotifierFeaturerId(), Notifier.class);
                notifier.notify(recipientIds, context, channelEvent.getEventCode(), notificationChannel.getNotifierParams());
            }

            historyNotificationEntity
                    .setStatusId(HistoryNotificationTaskStatus.SENT)
                    .setStatusDetails(STR."\{recipientsCount} recipients were notified")
                    .setDoneAt(Timestamp.from(Instant.now()));
        } catch (ServiceException e) {
            log.error(e.log());
            historyNotificationEntity
                    .setStatusId(HistoryNotificationTaskStatus.FAILED)
                    .setStatusDetails(e.log());
        } catch (Throwable e) {
            log.error("Exception: ", e);
            historyNotificationEntity
                    .setStatusId(HistoryNotificationTaskStatus.FAILED)
                    .setStatusDetails(e.getMessage());
        } finally {
            historyNotificationTaskRepository.save(historyNotificationEntity);
            LoggerUtils.cleanMDC();
        }
    }

    public Set<UUID> recipientResolve(HistoryNotificationRecipientEntity notificationRecipient, HistoryEntity history) throws ServiceException {
        RecipientResolver recipientResolver = featurerService.getFeaturer(notificationRecipient.getRecipientResolverFeaturer(), RecipientResolver.class);
        return recipientResolver.resolve(history, notificationRecipient.getRecipientResolverParams());
    }

    public Map<String, String> collectHistoryContext(UUID contextId, HistoryEntity history) throws ServiceException {
        Map<String, String> context = new HashMap<>();
        for (NotificationContextCollectorEntity contextCollector : notificationContextService.getContextCollectors(contextId)) {
            ContextCollector collector = featurerService.getFeaturer(contextCollector.getContextCollectorFeaturer(), ContextCollector.class);
            context = collector.collectData(history, context, contextCollector.getContextCollectorParams());
        }
        return context;
    }

    private Map<String, String> getContext(UUID contextId, HistoryEntity history) throws ServiceException {
        if (contextCache.containsKey(contextId))
            return contextCache.get(contextId);
        Map<String, String> context = collectHistoryContext(contextId, history);
        contextCache.put(contextId, context);
        return context;
    }
}
