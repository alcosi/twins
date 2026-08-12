package org.twins.core.service.notification;

import io.github.breninsul.logging.aspect.JavaLoggingLevel;
import io.github.breninsul.logging.aspect.annotation.LogExecutionTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.FeaturerService;
import org.cambium.service.EntitySecureFindServiceImpl;
import org.cambium.service.EntitySmartService;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.twins.core.dao.history.HistoryEntity;
import org.twins.core.dao.notification.HistoryNotificationEntity;
import org.twins.core.dao.notification.NotificationContextCollectorEntity;
import org.twins.core.dao.notification.NotificationContextEntity;
import org.twins.core.dao.notification.NotificationContextRepository;
import org.twins.core.featurer.notificator.context.ContextCollector;
import org.twins.core.featurer.notificator.context.ContextCollectorBatch;
import org.twins.core.service.domain.DomainUserService;
import org.twins.core.service.i18n.I18nService;

import java.util.*;
import java.util.function.Function;

@Slf4j
@Service
@Lazy
@LogExecutionTime(logPrefix = "LONG EXECUTION TIME:", logIfTookMoreThenMs = 2 * 1000, level = JavaLoggingLevel.WARNING)
@RequiredArgsConstructor
public class NotificationContextService extends EntitySecureFindServiceImpl<NotificationContextEntity> {
    private final FeaturerService featurerService;
    private final NotificationContextRepository notificationContextRepository;
    private final NotificationContextCollectorService notificationContextCollectorService;
    private final I18nService i18nService;
    private final DomainUserService domainUserService;

    @Override
    public CrudRepository<NotificationContextEntity, UUID> entityRepository() {
        return notificationContextRepository;
    }

    @Override
    public Function<NotificationContextEntity, UUID> entityGetIdFunction() {
        return NotificationContextEntity::getId;
    }

    @Override
    public boolean isEntityReadDenied(NotificationContextEntity entity, EntitySmartService.ReadPermissionCheckMode readPermissionCheckMode) throws ServiceException {
        return false;
    }

    @Override
    public boolean validateEntity(NotificationContextEntity entity, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
        return true;
    }

    public List<NotificationContextCollectorEntity> getContextCollectors(UUID contextId) {
        return notificationContextCollectorService.findByContextId(contextId);
    }

    /**
     * Chunk-level batch context collection, symmetric to {@code resolveRecipientsBatch}: one
     * {@link ContextCollector#collectDataBatch} per {@code (contextCollectorFeaturerId, params)} group per
     * contextId, so DB-backed collectors preload once per group. i18n (4911) is two-phase — collectors
     * register ids via {@link ContextCollectorBatch#addI18n}, then translations are resolved in bulk per
     * locale (the twin creator's locale, loaded once for the chunk). Results land on each
     * {@code task.collectedContextByContextId} for processTask.
     */
    public void collectHistoryContextBatch(HistoryNotificationChunk chunk) throws ServiceException {
        if (chunk == null || chunk.getTasksByConfig().isEmpty()) {
            return;
        }
        UUID domainId = chunk.getDomainId();
        // contextId → set of histories that need it (via config → channelEvent → notificationContextId)
        Map<UUID, Set<HistoryEntity>> historiesByContextId = new HashMap<>();
        for (var e : chunk.getTasksByConfig().entrySet()) {
            HistoryNotificationEntity config = e.getKey();
            if (config.getNotificationChannelEvent() == null) {
                continue;
            }
            UUID contextId = config.getNotificationChannelEvent().getNotificationContextId();
            if (contextId == null) {
                continue;
            }
            Set<HistoryEntity> histories = historiesByContextId.computeIfAbsent(contextId, _ -> new HashSet<>());
            for (var task : e.getValue()) {
                if (task.getHistory() != null) {
                    histories.add(task.getHistory());
                }
            }
        }
        if (historiesByContextId.isEmpty()) {
            return;
        }

        // one ContextCollectorBatch per contextId; run each contextId's collectors (grouped by params) once
        Map<UUID, ContextCollectorBatch> batchByContextId = new HashMap<>();
        for (var e : historiesByContextId.entrySet()) {
            UUID contextId = e.getKey();
            ContextCollectorBatch batch = new ContextCollectorBatch(domainId);
            for (HistoryEntity history : e.getValue()) {
                batch.add(history);
            }
            for (NotificationContextCollectorEntity contextCollector : getContextCollectors(contextId)) {
                ContextCollector collector = featurerService.getFeaturer(contextCollector.getContextCollectorFeaturerId(), ContextCollector.class);
                collector.collectDataBatch(batch, contextCollector.getContextCollectorParams());
            }
            batchByContextId.put(contextId, batch);
        }

        // resolve i18n for every batch — per-history locale = twin creator's locale (loaded once for the chunk)
        Map<UUID, Locale> localeByUser = loadCreatorLocales(chunk);
        for (ContextCollectorBatch batch : batchByContextId.values()) {
            resolveI18n(batch, history -> creatorLocale(history, localeByUser));
        }

        // distribute per-(task, contextId) → task-entity.collectedContextByContextId
        for (var e : chunk.getConfigsByTask().entrySet()) {
            var task = e.getKey();
            HistoryEntity history = task.getHistory();
            Map<UUID, Map<String, String>> byContextId = new HashMap<>();
            if (history != null) {
                for (HistoryNotificationEntity config : e.getValue()) {
                    if (config.getNotificationChannelEvent() == null) {
                        continue;
                    }
                    UUID contextId = config.getNotificationChannelEvent().getNotificationContextId();
                    byContextId.computeIfAbsent(contextId, cid -> {
                        ContextCollectorBatch batch = batchByContextId.get(cid);
                        return batch != null && batch.getContextByHistory().get(history) != null
                                ? new HashMap<>(batch.getContextByHistory().get(history))
                                : new HashMap<>();
                    });
                }
            }
            task.setCollectedContextByContextId(byContextId);
        }
    }

    /** Bulk-load the twin-creator locales for the chunk (one query) → userId → locale. */
    private Map<UUID, Locale> loadCreatorLocales(HistoryNotificationChunk chunk) {
        return domainUserService.getLocaleMap(chunk.getDomainId(), chunk.getCreatedByUserIds());
    }

    private static Locale creatorLocale(HistoryEntity history, Map<UUID, Locale> localeByUser) {
        if (history == null || history.getTwin() == null) {
            return null;
        }
        return localeByUser.get(history.getTwin().getCreatedByUserId());
    }

    /**
     * Substitute i18n placeholders accumulated in {@code batch} with translations resolved per locale.
     * {@code localeByHistory} provides the locale for each history (batch resolves them in bulk upstream;
     * single-history callers pass a fallback that yields the thread-local locale).
     */
    private void resolveI18n(ContextCollectorBatch batch, Function<HistoryEntity, Locale> localeByHistory) throws ServiceException {
        // grouped by locale so one bulk translate fires per locale
        Map<Locale, Set<UUID>> idsByLocale = new HashMap<>();
        for (HistoryEntity history : batch.getContextByHistory().keySet()) {
            Locale locale = localeByHistory.apply(history);
            if (locale != null) {
                idsByLocale.computeIfAbsent(locale, _ -> new HashSet<>());
            }
        }
        // assign each accumulated i18n id to the locale of every history it references
        for (var e : batch.getI18nRefs().entrySet()) {
            for (ContextCollectorBatch.I18nRef ref : e.getValue()) {
                Locale locale = localeByHistory.apply(ref.history());
                if (locale != null) {
                    idsByLocale.get(locale).add(e.getKey());
                }
            }
        }
        // bulk translate per locale and substitute into the context entries
        for (var e : idsByLocale.entrySet()) {
            Locale locale = e.getKey();
            if (e.getValue().isEmpty()) {
                continue;
            }
            Map<UUID, String> translations = i18nService.translateToLocale(e.getValue(), locale);
            for (var refEntry : batch.getI18nRefs().entrySet()) {
                String translation = translations.get(refEntry.getKey());
                if (translation == null) {
                    continue;
                }
                for (ContextCollectorBatch.I18nRef ref : refEntry.getValue()) {
                    if (locale.equals(localeByHistory.apply(ref.history()))) {
                        batch.getContextByHistory().get(ref.history()).put(ref.contextKey(), translation);
                    }
                }
            }
        }
    }
}
