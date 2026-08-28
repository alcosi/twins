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
     * resolved in bulk per RECIPIENT locale (each recipient gets the notification in their own
     * locale, not the twin creator's). Per-(context, history) results are merged from the group
     * batches and land on each {@code task.collectedContextByContextId} as a TEMPLATE with
     * {@code #i18n=<uuid>} placeholders — materialized per recipient locale at notify-event build
     * time via {@link #materializeContext}.
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

        // resolve i18n for the whole chunk in the RECIPIENTS' locales: recipient locales are
        // bulk-loaded once (one query), translations bulk-loaded once per distinct locale across all
        // group batches (groups of one chunk usually share the same i18n ids, e.g. twin class names).
        // Placeholders stay in the templates — materialization happens per locale at event build
        loadRecipientLocalesAndTranslations(chunk, groupBatchByKey.values());

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
     * Bulk-loads the chunk recipients' locales (one query) and the i18n translations for every
     * distinct recipient locale (one bulk translate per locale) into the chunk. Recipients without a
     * locale end up in the null-locale group — their placeholders materialize as empty strings.
     */
    private void loadRecipientLocalesAndTranslations(HistoryNotificationChunk chunk, Collection<ContextCollectorBatch> batches) throws ServiceException {
        // place the raw placeholders into the per-history context templates: addI18n only registers the
        // ref, the value materializes per recipient locale at event build (materializeContext)
        for (ContextCollectorBatch batch : batches) {
            for (var e : batch.getI18nRefs().entrySet()) {
                for (ContextCollectorBatch.I18nRef ref : e.getValue()) {
                    batch.getContextByHistory().get(ref.history()).put(ref.contextKey(), I18N_PLACEHOLDER_PREFIX + e.getKey());
                }
            }
        }
        Set<UUID> i18nIds = new HashSet<>();
        for (ContextCollectorBatch batch : batches) {
            i18nIds.addAll(batch.getI18nIds());
        }
        if (i18nIds.isEmpty()) {
            return;
        }
        chunk.getLocaleByRecipient().putAll(domainUserService.getLocaleMap(chunk.getDomainId(), chunk.getRecipientUserIds()));
        for (Locale locale : new HashSet<>(chunk.getLocaleByRecipient().values())) {
            chunk.getI18nTranslationsByLocale()
                    .computeIfAbsent(locale, _ -> i18nService.translateToLocale(i18nIds, locale));
        }
    }

    /**
     * Materializes a context template for one locale: {@code #i18n=<uuid>} placeholder values are
     * replaced with the locale's translation (empty string on a missing translation or a null locale —
     * parity with the old per-id translateToLocale). Non-placeholder values pass through as is.
     */
    public Map<String, String> materializeContext(Map<String, String> template, HistoryNotificationChunk chunk, Locale locale) {
        Map<String, String> context = new HashMap<>(template);
        for (var entry : context.entrySet()) {
            String value = entry.getValue();
            if (value == null || !value.startsWith(I18N_PLACEHOLDER_PREFIX)) {
                continue;
            }
            UUID i18nId = UUID.fromString(value.substring(I18N_PLACEHOLDER_PREFIX.length()));
            Map<UUID, String> translations = locale != null ? chunk.getI18nTranslationsByLocale().get(locale) : null;
            String translation = translations != null ? translations.get(i18nId) : null;
            entry.setValue(translation != null ? translation : "");
        }
        return context;
    }

    private static final String I18N_PLACEHOLDER_PREFIX = "#i18n=";
}
