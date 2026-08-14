package org.twins.core.service.notification;

import io.github.breninsul.logging.aspect.JavaLoggingLevel;
import io.github.breninsul.logging.aspect.annotation.LogExecutionTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.FeaturerGroup;
import org.cambium.featurer.FeaturerService;
import org.cambium.service.EntitySecureFindServiceImpl;
import org.cambium.service.EntitySmartService;
import org.jetbrains.annotations.NotNull;
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
     * {@link ContextCollector#collectDataBatch} per {@code (contextCollectorFeaturerId, params)} group
     * across the WHOLE chunk — different contexts often share collector configs (e.g. a head-twin
     * collector in both manager and worker notification contexts), so a group runs once over the
     * union of its histories (one beforeCollect preload per group). i18n (4911) is two-phase —
     * collectors register ids via {@link ContextCollectorBatch#addI18n}, then translations are
     * resolved in bulk per locale (the twin creator's locale, loaded once for the chunk).
     * Per-(context, history) results are merged from the group batches and land on each
     * {@code task.collectedContextByContextId} for processTask.
     */
    public void collectHistoryContextBatch(HistoryNotificationChunk chunk) throws ServiceException {
        if (chunk == null || chunk.getTasksByConfig().isEmpty()) {
            return;
        }
        // channel events carrying a context (one pass over matched configs)
        Set<NotificationChannelEventEntity> channelEvents = getNotificationChannelEvents(chunk);
        if (channelEvents.isEmpty()) {
            return;
        }
        // bulk-load context collectors onto channel events (one query for all distinct contextIds)
        notificationChannelEventService.loadContextCollectors(channelEvents);

        // one pass over configs: (collector, history) pairs straight into chunk-global
        // (featurerId, params) groups; side map contextId → its groups' configKeys (for the merge)
        UUID domainId = chunk.getDomainId();
        var collectorGroups = FeaturerGroup.builder(
                NotificationContextCollectorEntity::getContextCollectorFeaturerId,
                NotificationContextCollectorEntity::getContextCollectorParams,
                HistoryEntity.class);
        Map<UUID, Set<String>> groupKeysByContextId = new HashMap<>();
        for (var e : chunk.getTasksByConfig().entrySet()) {
            HistoryNotificationEntity config = e.getKey();
            NotificationChannelEventEntity channelEvent = config.getNotificationChannelEvent();
            if (channelEvent == null || channelEvent.getNotificationContextId() == null || channelEvent.getCollectors() == null) {
                continue;
            }
            UUID contextId = channelEvent.getNotificationContextId();
            for (NotificationContextCollectorEntity collector : channelEvent.getCollectors().getList()) {
                for (HistoryNotificationTaskEntity task : e.getValue()) {
                    collectorGroups.add(collector, task.getHistory());
                }
                groupKeysByContextId.computeIfAbsent(contextId, _ -> new LinkedHashSet<>())
                        .add(FeaturerService.toConfigKey(collector.getContextCollectorFeaturerId(), collector.getContextCollectorParams()));
            }
        }

        // run each group ONCE: per-group batch over the group's histories (histories repeated by
        // contexts sharing the group are absorbed by the idempotent batch.add) — one
        // collectDataBatch + one beforeCollect preload per unique (featurerId, params) in the chunk
        Map<String, ContextCollectorBatch> groupBatchByKey = new HashMap<>();
        for (var group : collectorGroups.build()) {
            ContextCollector collector = featurerService.getFeaturer(group.getFeaturerId(), ContextCollector.class);
            ContextCollectorBatch batch = new ContextCollectorBatch(domainId);
            for (HistoryEntity history : group.getItems()) {
                batch.add(history);
            }
            collector.collectDataBatch(batch, group.getParams());
            groupBatchByKey.put(group.getConfigKey(), batch);
        }

        // resolve i18n for the whole chunk: translations are bulk-loaded ONCE per locale across all
        // group batches (groups of one chunk usually share the same i18n ids, e.g. twin class
        // names), then substituted in one pass per batch
        Map<UUID, Locale> localeByUser = loadCreatorLocales(chunk);
        resolveI18n(groupBatchByKey.values(), localeByUser);

        // distribute per-(task, contextId): a context's entry for a history = union of its groups'
        // contributions (collectors complement the same per-history context map)
        for (var e : chunk.getConfigsByTask().entrySet()) {
            var task = e.getKey();
            HistoryEntity history = task.getHistory();
            Map<UUID, Map<String, String>> byContextId = new HashMap<>();
            for (HistoryNotificationEntity config : e.getValue()) {
                UUID contextId = config.getNotificationChannelEvent().getNotificationContextId();
                byContextId.computeIfAbsent(contextId,
                        _ -> collectContext(groupKeysByContextId.get(contextId), groupBatchByKey, history));
            }
            task.setCollectedContextByContextId(byContextId);
        }
    }

    @NotNull
    private static Set<NotificationChannelEventEntity> getNotificationChannelEvents(HistoryNotificationChunk chunk) {
        Set<NotificationChannelEventEntity> channelEvents = new HashSet<>();
        for (HistoryNotificationEntity config : chunk.getTasksByConfig().keySet()) {
            if (config.getNotificationChannelEvent() != null
                    && config.getNotificationChannelEvent().getNotificationContextId() != null) {
                channelEvents.add(config.getNotificationChannelEvent());
            }
        }
        return channelEvents;
    }

    /**
     * Union of the context's collector groups' contributions for one history (fresh mutable map).
     */
    private static Map<String, String> collectContext(Set<String> groupKeys,
                                                      Map<String, ContextCollectorBatch> groupBatchByKey,
                                                      HistoryEntity history) {
        Map<String, String> context = new HashMap<>();
        if (groupKeys == null) {
            return context;
        }
        for (String groupKey : groupKeys) {
            ContextCollectorBatch batch = groupBatchByKey.get(groupKey);
            if (batch == null) {
                continue;
            }
            Map<String, String> collected = batch.getContextByHistory().get(history);
            if (collected != null) {
                context.putAll(collected);
            }
        }
        return context;
    }

    /**
     * Bulk-load the twin-creator locales for the chunk (one query) → userId → locale.
     */
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
