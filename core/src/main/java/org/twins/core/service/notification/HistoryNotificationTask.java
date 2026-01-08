package org.twins.core.service.notification;

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
import org.twins.core.dao.notification.*;
import org.twins.core.enums.HistoryNotificationTaskStatus;
import org.twins.core.enums.history.HistoryType;
import org.twins.core.featurer.notificator.notifier.Notifier;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.history.HistoryRecipientService;
import org.twins.core.service.twin.TwinValidatorSetService;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
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
    private HistoryNotificationSchemaMapRepository historyNotificationSchemaMapEntityRepository;
    @Autowired
    private HistoryNotificationTaskRepository historyNotificationTaskRepository;
    @Autowired
    private FeaturerService featurerService;
    @Autowired
    private NotificationContextService notificationContextService;
    @Autowired
    private HistoryRecipientService historyRecipientService;
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
            HistoryEntity history = historyNotificationEntity.getHistory();
            LoggerUtils.logSession(history.getHistoryBatchId());
            LoggerUtils.logController("historyNotificationTask");
            LoggerUtils.logPrefix("HISTORY[" + historyNotificationEntity.getId() + "]:");
            log.info("Performing history notification task: {}", historyNotificationEntity.logDetailed());
            if (history.getTwin().getTwinClass().getDomainId() == null) {
                throw new NotificationSkippedException("Twin is out of domain");
            }
            var twin = history.getTwin();
            authService.setThreadLocalApiUser(twin.getTwinClass().getDomainId(), twin.getOwnerBusinessAccountId(), twin.getCreatedByUserId()); //todo not sure that it's correct to use creator
            List<HistoryNotificationSchemaMapEntity> configs = getConfigs(history);
            if (CollectionUtils.isEmpty(configs)) {
                throw new NotificationSkippedException("No configs found for " + history.logNormal());
            }
            KitGroupedObj<HistoryNotificationSchemaMapEntity, UUID, UUID, NotificationChannelEventEntity> notificationConfigsGroupedByChannelEvent = new KitGroupedObj<>(
                    configs,
                    HistoryNotificationSchemaMapEntity::getId,
                    HistoryNotificationSchemaMapEntity::getNotificationChannelEventId,
                    HistoryNotificationSchemaMapEntity::getNotificationChannelEvent);

            int recipientsCount = 0;
            int skippedDuplicatesCount = 0;
            for (var entry : notificationConfigsGroupedByChannelEvent.getGroupedMap().entrySet()) {
                var channelEvent = notificationConfigsGroupedByChannelEvent.getGroupingObject(entry.getKey());
                if (channelEvent.isUniqueInBatch()) {
                    Set<String> processedEvents = batchEventCache.get(history.getHistoryBatchId(), k -> ConcurrentHashMap.newKeySet());
                    if (processedEvents != null && !processedEvents.add(channelEvent.getEventCode())) {
                        log.info("Notification for event {} in batch {} skipped due to uniqueInBatch flag", channelEvent.getEventCode(), history.getHistoryBatchId());
                        skippedDuplicatesCount++;
                        continue;
                    }
                }

                var recipientIds = new HashSet<UUID>();
                for (var config : entry.getValue()) {
                    if (twinValidatorSetService.isValid(history.getTwin(), config)) {
                        recipientIds.addAll(historyRecipientService.recipientResolve(config.getHistoryNotificationRecipient().getId(), history));
                    }
                }
                if (recipientIds.isEmpty())
                    continue;
                recipientsCount += recipientIds.size();
                var context = getContext(channelEvent.getNotificationContextId(), history);
                NotificationChannelEntity notificationChannel = channelEvent.getNotificationChannel();
                Notifier notifier = featurerService.getFeaturer(notificationChannel.getNotifierFeaturerId(), Notifier.class);
                notifier.notify(recipientIds, context, channelEvent.getEventCode(), notificationChannel.getNotifierParams());
            }

            if (skippedDuplicatesCount > 0) {
                throw new NotificationSkippedException("Notification for batch " + history.getHistoryBatchId() + " skipped due to uniqueInBatch flag");
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

    private List<HistoryNotificationSchemaMapEntity> getConfigs(HistoryEntity history) {
        List<HistoryNotificationSchemaMapEntity> configs;
        HistoryType historyType = history.getHistoryType();
        if (history.getTwinClassFieldId() == null) {
            configs = historyNotificationSchemaMapEntityRepository
                    .findByHistoryTypeIdAndTwinClassIdAndNotificationSchemaId(
                            historyType.name(),
                            history.getTwin().getTwinClassId(),
                            historyNotificationEntity.getNotificationSchemaId());
        } else {
            configs = historyNotificationSchemaMapEntityRepository
                    .findByHistoryTypeIdAndTwinClassIdAndTwinClassFieldIdAndNotificationSchemaId(
                            historyType.name(),
                            history.getTwin().getTwinClassId(),
                            history.getTwinClassFieldId(),
                            historyNotificationEntity.getNotificationSchemaId());
        }
        return configs;
    }

    private Map<String, String> getContext(UUID contextId, HistoryEntity history) throws ServiceException {
        if (contextCache.containsKey(contextId))
            return contextCache.get(contextId);
        Map<String, String> context = notificationContextService.collectHistoryContext(contextId, history);
        contextCache.put(contextId, context);
        return context;
    }

    private static class NotificationSkippedException extends RuntimeException {
        public NotificationSkippedException(String message) {
            super(message);
        }
    }
}
