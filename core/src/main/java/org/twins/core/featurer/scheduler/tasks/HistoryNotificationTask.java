package org.twins.core.featurer.scheduler.tasks;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.kit.KitGroupedObj;
import org.cambium.common.util.CollectionUtils;
import org.cambium.common.util.LoggerUtils;
import org.cambium.featurer.FeaturerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.twins.core.dao.history.HistoryEntity;
import org.twins.core.dao.notification.HistoryNotificationEntity;
import org.twins.core.dao.notification.HistoryNotificationRepository;
import org.twins.core.dao.notification.HistoryNotificationTaskEntity;
import org.twins.core.dao.notification.HistoryNotificationTaskRepository;
import org.twins.core.enums.HistoryNotificationTaskStatus;
import org.twins.core.featurer.notificator.notifier.Notifier;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.history.HistoryRecipientService;
import org.twins.core.service.notification.HistoryNotificationService;
import org.twins.core.service.notification.NotificationContextService;
import org.twins.core.service.twin.TwinValidatorSetService;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Component
@Scope("prototype")
@Slf4j
public class HistoryNotificationTask implements Runnable {
    private final HistoryNotificationTaskEntity historyNotificationEntity;
    @Autowired
    private HistoryNotificationRepository historyNotificationRepository;
    @Autowired
    private HistoryNotificationTaskRepository historyNotificationTaskRepository;
    @Autowired
    private FeaturerService featurerService;
    @Autowired
    private NotificationContextService notificationContextService;
    @Autowired
    private HistoryRecipientService historyRecipientService;
    @Autowired
    private HistoryNotificationService historyNotificationService;
    @Autowired
    private TwinValidatorSetService twinValidatorSetService;
    @Autowired
    private AuthService authService;

    private final Map<UUID, Map<String, String>> contextCache = new HashMap<>();
    private static final Cache<UUID, Set<String>> batchEventCache = Caffeine.newBuilder()
            .expireAfterAccess(1, TimeUnit.MINUTES)
            .build();

    public HistoryNotificationTask(HistoryNotificationTaskEntity historyNotificationEntity) {
        this.historyNotificationEntity = historyNotificationEntity;
    }

    @Override
    public void run() {
        try {
            var history = historyNotificationEntity.getHistory();
            var twin = history.getTwin();

            LoggerUtils.logSession(history.getHistoryBatchId());
            LoggerUtils.logController("historyNotificationTask");
            LoggerUtils.logPrefix("HISTORY[" + historyNotificationEntity.getId() + "]:");
            log.info("Performing history notification task: {}", historyNotificationEntity.logDetailed());

            if (twin.getTwinClass().getDomainId() == null) {
                throw new NotificationSkippedException("Twin is out of domain");
            }

            authService.setThreadLocalApiUser(twin.getTwinClass().getDomainId(), twin.getOwnerBusinessAccountId(), twin.getCreatedByUserId());

            var configs = getConfigs(history);
            if (CollectionUtils.isEmpty(configs)) {
                throw new NotificationSkippedException("No configs found for " + history.logNormal());
            }

            historyNotificationService.loadNotificationChannelEvent(configs);
            historyNotificationService.loadHistoryNotificationRecipient(configs);
            var recipients = configs.stream()
                    .map(HistoryNotificationEntity::getHistoryNotificationRecipient)
                    .filter(Objects::nonNull)
                    .toList();
            historyRecipientService.loadCollectors(recipients);

            var notificationConfigsGroupedByChannelEvent = new KitGroupedObj<>(
                    configs,
                    HistoryNotificationEntity::getId,
                    HistoryNotificationEntity::getNotificationChannelEventId,
                    HistoryNotificationEntity::getNotificationChannelEvent
            );

            var recipientsCount = 0;
            for (var entry : notificationConfigsGroupedByChannelEvent.getGroupedMap().entrySet()) {
                var channelEvent = notificationConfigsGroupedByChannelEvent.getGroupingObject(entry.getKey());

                if (channelEvent.isUniqueInBatch()) {
                    var processedEvents = batchEventCache.get(history.getHistoryBatchId(), k -> ConcurrentHashMap.newKeySet());
                    if (processedEvents != null && !processedEvents.add(channelEvent.getEventCode())) {
                        log.info("Notification for event {} in batch {} skipped due to uniqueInBatch flag", channelEvent.getEventCode(), history.getHistoryBatchId());
                        continue;
                    }
                }

                var recipientIds = new HashSet<UUID>();
                for (var config : entry.getValue()) {
                    if (twinValidatorSetService.isValid(history.getTwin(), config)) {
                        // todo create mechanism to group recipient resolvers and call batch resolve
                        recipientIds.addAll(historyRecipientService.recipientResolve(config.getHistoryNotificationRecipient(), history));
                    }
                }

                if (recipientIds.isEmpty()) {
                    continue;
                }

                recipientsCount += recipientIds.size();
                var context = getContext(channelEvent.getNotificationContextId(), history);
                var notificationChannel = channelEvent.getNotificationChannel();
                var notifier = featurerService.getFeaturer(notificationChannel.getNotifierFeaturerId(), Notifier.class);
                notifier.notify(recipientIds, context, channelEvent.getEventCode(), notificationChannel.getNotifierParams());
            }

            if (recipientsCount == 0) {
                throw new NotificationSkippedException("No recipients were found for " + history.logNormal());
            }

            historyNotificationEntity
                    .setStatusId(HistoryNotificationTaskStatus.SENT)
                    .setStatusDetails(STR."\{recipientsCount} recipients were notified")
                    .setDoneAt(Timestamp.from(Instant.now()));
        } catch (NotificationSkippedException e) {
            log.info(e.getMessage());

            historyNotificationEntity
                    .setStatusId(HistoryNotificationTaskStatus.SKIPPED)
                    .setStatusDetails(e.getMessage());
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
            authService.removeThreadLocalApiUser();
            LoggerUtils.cleanMDC();
            historyNotificationTaskRepository.save(historyNotificationEntity);
        }
    }

    private List<HistoryNotificationEntity> getConfigs(HistoryEntity history) {
        Set<UUID> matchingClassIds = new HashSet<>(history.getTwin().getTwinClass().getExtendedClassIdSet());
        matchingClassIds.add(history.getTwin().getTwinClassId());

        if (history.getTwinClassFieldId() == null) {
            return historyNotificationRepository.findByHistoryTypeIdAndTwinClassIdInAndNotificationSchemaId(
                    history.getHistoryType().name(),
                    matchingClassIds,
                    historyNotificationEntity.getNotificationSchemaId());
        } else {
            return historyNotificationRepository.findByHistoryTypeIdAndTwinClassIdInAndTwinClassFieldIdAndNotificationSchemaId(
                    history.getHistoryType().name(),
                    matchingClassIds,
                    history.getTwinClassFieldId(),
                    historyNotificationEntity.getNotificationSchemaId());
        }
    }

    private Map<String, String> getContext(UUID contextId, HistoryEntity history) throws ServiceException {
        if (contextCache.containsKey(contextId)) {
            return contextCache.get(contextId);
        }

        var context = notificationContextService.collectHistoryContext(contextId, history);
        contextCache.put(contextId, context);

        return context;
    }

    private static class NotificationSkippedException extends RuntimeException {
        public NotificationSkippedException(String message) {
            super(message);
        }
    }
}
