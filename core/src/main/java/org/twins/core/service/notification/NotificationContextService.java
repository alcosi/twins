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
import org.twins.core.dao.notification.*;
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
    @Lazy
    private final NotificationChannelEventService notificationChannelEventService;

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
        Set<NotificationChannelEventEntity> channelEvents = new HashSet<>();
        for (var e : chunk.getTasksByConfig().entrySet()) {
            HistoryNotificationEntity config = e.getKey();
            if (config.getNotificationChannelEvent() == null) {
                continue;
            }
            channelEvents.add(config.getNotificationChannelEvent());
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

        // bulk-load context collectors onto channel events (one query for all distinct contextIds)
        // → contextId → collectors map (events sharing a context share the same loaded kit)
        notificationChannelEventService.loadContextCollectors(channelEvents);
        Map<UUID, List<NotificationContextCollectorEntity>> collectorsByContextId = new HashMap<>();
        for (NotificationChannelEventEntity channelEvent : channelEvents) {
            UUID contextId = channelEvent.getNotificationContextId();
            if (contextId != null && channelEvent.getCollectors() != null) {
                collectorsByContextId.putIfAbsent(contextId, channelEvent.getCollectors().getList());
            }
        }

        // one ContextCollectorBatch per contextId; collectors grouped by (featurerId, params) so each runs
        // once for all its histories (via FeaturerService.groupByFeaturerParams — symmetric to resolveRecipientsBatch)
        Map<UUID, ContextCollectorBatch> batchByContextId = new HashMap<>();
        for (var e : historiesByContextId.entrySet()) {
            UUID contextId = e.getKey();
            Set<HistoryEntity> histories = e.getValue();
            List<NotificationContextCollectorEntity> contextCollectors = collectorsByContextId.getOrDefault(contextId, List.of());
            if (contextCollectors.isEmpty()) {
                continue;
            }
            // work units: (collector-entity, history) — grouped by featurer params below
            List<ContextCollectorWork> work = new ArrayList<>();
            for (NotificationContextCollectorEntity contextCollector : contextCollectors) {
                if (contextCollector.getContextCollectorFeaturerId() == null) {
                    continue;
                }
                for (HistoryEntity history : histories) {
                    work.add(new ContextCollectorWork(contextCollector, history));
                }
            }
            ContextCollectorBatch batch = new ContextCollectorBatch(domainId);
            for (ContextCollectorWork w : work) {
                batch.add(w.history());
            }
            // one collectDataBatch per (featurerId, params) group — getFeaturer + extractProperties once per group
            for (var group : FeaturerService.groupByFeaturerParams(work,
                    w -> w.collector().getContextCollectorFeaturerId(),
                    w -> w.collector().getContextCollectorParams())) {
                ContextCollector collector = featurerService.getFeaturer(group.getFeaturerId(), ContextCollector.class);
                collector.collectDataBatch(batch, group.getParams());
            }
            batchByContextId.put(contextId, batch);
        }

        // resolve i18n for the whole chunk: translations are bulk-loaded ONCE per locale across all
        // context batches (contexts of one chunk usually share the same i18n ids, e.g. twin class
        // names), then substituted in one pass per batch
        Map<UUID, Locale> localeByUser = loadCreatorLocales(chunk);
        resolveI18n(batchByContextId.values(), localeByUser);

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

    /** A (context-collector-entity, history) pair — the work unit grouped by featurer params. */
    private record ContextCollectorWork(NotificationContextCollectorEntity collector, HistoryEntity history) {
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
     * Substitute i18n placeholders accumulated in the chunk's context batches with translations
     * resolved per locale. Each history's locale is the twin creator's locale from {@code localeByUser}
     * (loaded once for the chunk); ONE bulk translate fires per locale across all batches, then
     * placeholders are filled in a single pass over each batch's refs.
     */
    private void resolveI18n(Collection<ContextCollectorBatch> batches, Map<UUID, Locale> localeByUser) throws ServiceException {
        // 1. group i18n ids by locale across ALL batches (one pass over refs) — the union naturally
        //    dedups ids shared between contexts of one chunk
        Map<Locale, Set<UUID>> idsByLocale = new HashMap<>();
        for (ContextCollectorBatch batch : batches) {
            for (var e : batch.getI18nRefs().entrySet()) {
                for (ContextCollectorBatch.I18nRef ref : e.getValue()) {
                    Locale locale = creatorLocale(ref.history(), localeByUser);
                    if (locale != null) {
                        idsByLocale.computeIfAbsent(locale, _ -> new HashSet<>()).add(e.getKey());
                    }
                }
            }
        }
        // 2. one bulk translate per locale (chunk-wide, shared by all context batches)
        Map<Locale, Map<UUID, String>> translationsByLocale = new HashMap<>();
        for (var e : idsByLocale.entrySet()) {
            translationsByLocale.put(e.getKey(), i18nService.translateToLocale(e.getValue(), e.getKey()));
        }
        // 3. fill placeholders in one pass per batch (translation picked by the history's locale)
        for (ContextCollectorBatch batch : batches) {
            for (var e : batch.getI18nRefs().entrySet()) {
                UUID i18nId = e.getKey();
                for (ContextCollectorBatch.I18nRef ref : e.getValue()) {
                    Locale locale = creatorLocale(ref.history(), localeByUser);
                    Map<UUID, String> translations = locale != null ? translationsByLocale.get(locale) : null;
                    if (translations == null) {
                        continue;
                    }
                    String translation = translations.get(i18nId);
                    if (translation != null) {
                        batch.getContextByHistory().get(ref.history()).put(ref.contextKey(), translation);
                    }
                }
            }
        }
    }
}
