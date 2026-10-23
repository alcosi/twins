package org.twins.core.service.context;

import io.github.breninsul.logging.aspect.JavaLoggingLevel;
import io.github.breninsul.logging.aspect.annotation.LogExecutionTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.FeaturerGroup;
import org.cambium.featurer.FeaturerService;
import org.cambium.service.EntitySecureFindServiceImpl;
import org.cambium.service.EntitySmartService;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.twins.core.dao.context.ContextCollectorEntity;
import org.twins.core.dao.context.ContextEntity;
import org.twins.core.dao.context.ContextRepository;
import org.twins.core.dao.history.HistoryEntity;
import org.twins.core.dao.notification.HistoryNotificationEntity;
import org.twins.core.dao.notification.HistoryNotificationTaskEntity;
import org.twins.core.dao.notification.NotificationChannelEventEntity;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.featurer.context.ContextCollector;
import org.twins.core.featurer.context.ContextCollectorBatch;
import org.twins.core.holder.I18nCacheHolder;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.domain.DomainUserService;
import org.twins.core.service.i18n.I18nService;
import org.twins.core.service.notification.HistoryNotificationChunk;
import org.twins.core.service.notification.NotificationChannelEventService;

import java.util.*;
import java.util.function.Function;

@Slf4j
@Service
@Lazy
@LogExecutionTime(logPrefix = "LONG EXECUTION TIME:", logIfTookMoreThenMs = 2 * 1000, level = JavaLoggingLevel.WARNING)
@RequiredArgsConstructor
public class ContextService extends EntitySecureFindServiceImpl<ContextEntity> {
    private final FeaturerService featurerService;
    private final ContextRepository contextRepository;
    private final ContextCollectorService contextCollectorService;
    private final I18nService i18nService;
    private final DomainUserService domainUserService;
    @Lazy
    private final NotificationChannelEventService notificationChannelEventService;
    @Lazy
    private final AuthService authService;

    @Override
    public CrudRepository<ContextEntity, UUID> entityRepository() {
        return contextRepository;
    }

    @Override
    public Function<ContextEntity, UUID> entityGetIdFunction() {
        return ContextEntity::getId;
    }

    @Override
    public boolean isEntityReadDenied(ContextEntity entity, EntitySmartService.ReadPermissionCheckMode readPermissionCheckMode) throws ServiceException {
        return false;
    }

    @Override
    public boolean validateEntity(ContextEntity entity, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
        return true;
    }

    public List<ContextCollectorEntity> getContextCollectors(UUID contextId) {
        return contextCollectorService.findByContextId(contextId);
    }

    /**
     * Chunk-level batch context collection, symmetric to {@code resolveRecipientsBatch}: one
     * {@link ContextCollector#collectDataBatch} per {@code (contextCollectorFeaturerId, params)} group
     * across the WHOLE chunk — different contexts often share collector configs (e.g. a head-twin
     * collector in both manager and worker notification contexts), so a group runs once over the
     * union of its histories (one beforeCollect preload per group). i18n (4911) is two-phase —
     * collectors put {@code #i18n=<uuid>} placeholders via {@link ContextCollectorBatch#addI18n},
     * then {@link #resolveI18n} bulk-translates the ids in the RECIPIENTS' locales (each recipient
     * gets the notification in their own locale, not the twin creator's). Per-(context, history)
     * results are merged from the group batches and land on each {@code task.collectedContextByContextId}
     * as a TEMPLATE still carrying the placeholders — materialized per recipient locale at
     * notify-event build time via {@link #materializeContext}.
     */
    public void collectHistoryContextBatch(HistoryNotificationChunk chunk) throws ServiceException {
        if (chunk == null || chunk.getTasksByConfig().isEmpty()) {
            return;
        }
        // channel events carrying a context (one pass over matched configs)
        Set<NotificationChannelEventEntity> channelEvents = notificationChannelEventService.getNotificationChannelEvents(chunk);
        if (channelEvents.isEmpty()) {
            return;
        }
        // bulk-load context collectors onto channel events (one query for all distinct contextIds)
        notificationChannelEventService.loadContextCollectors(channelEvents);

        // one pass over configs: (collector, history) pairs straight into chunk-global
        // (featurerId, params) groups; side map contextId → its groups' configKeys (for the merge)
        UUID domainId = chunk.getDomainId();
        var collectorGroups = FeaturerGroup.builder(
                ContextCollectorEntity::getContextCollectorFeaturerId,
                ContextCollectorEntity::getContextCollectorParams,
                HistoryEntity.class);
        Map<UUID, Set<String>> groupKeysByContextId = new HashMap<>();
        for (var e : chunk.getTasksByConfig().entrySet()) {
            HistoryNotificationEntity config = e.getKey();
            NotificationChannelEventEntity channelEvent = config.getNotificationChannelEvent();
            if (channelEvent == null || channelEvent.getContextId() == null || channelEvent.getCollectors() == null) {
                continue;
            }
            UUID contextId = channelEvent.getContextId();
            for (ContextCollectorEntity collector : channelEvent.getCollectors().getList()) {
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

        // resolve i18n for the whole chunk in the RECIPIENTS' locales (one bulk translate per locale,
        // shared by all group batches); placeholders stay in the templates — materialization happens
        // per locale at event build
        resolveI18n(chunk, groupBatchByKey.values());

        // distribute per-(task, contextId): a context's entry for a history = union of its groups'
        // contributions (collectors complement the same per-root context map)
        for (var e : chunk.getConfigsByTask().entrySet()) {
            var task = e.getKey();
            HistoryEntity history = task.getHistory();
            Map<UUID, Map<String, String>> byContextId = new HashMap<>();
            for (HistoryNotificationEntity config : e.getValue()) {
                UUID contextId = config.getNotificationChannelEvent().getContextId();
                byContextId.computeIfAbsent(contextId,
                        _ -> collectContext(groupKeysByContextId.get(contextId), groupBatchByKey, history));
            }
            task.setCollectedContextByContextId(byContextId);
        }
    }

    /**
     * REST/face entry point: collects several contexts for a SINGLE root twin (e.g. the current request
     * twin). Collector configs of all requested contexts are grouped by {@code (featurerId, params)} and
     * each group runs one batch over the same root — shared configs collect once. i18n placeholder values
     * ({@code #i18n=<uuid>}) are registered in {@link I18nCacheHolder} and materialized by the response
     * pipeline in the request locale — no explicit translation here.
     *
     * @return contextId → collected params (contexts without collectors are absent; values may still
     * carry i18n placeholders — they are resolved at response serialization)
     */
    public Map<UUID, Map<String, String>> collectContextsForTwin(Collection<UUID> contextIds, TwinEntity rootTwin) throws ServiceException {
        if (contextIds == null || contextIds.isEmpty() || rootTwin == null) {
            return Map.of();
        }
        Collection<ContextCollectorEntity> collectors = contextCollectorService.findByContextIdIn(new HashSet<>(contextIds));
        if (collectors.isEmpty()) {
            return Map.of();
        }
        UUID domainId = authService.getApiUser().getDomainId();
        Map<UUID, Map<String, String>> result = new HashMap<>();
        for (var group : FeaturerGroup.groupByFeaturerParams(collectors,
                ContextCollectorEntity::getContextCollectorFeaturerId,
                ContextCollectorEntity::getContextCollectorParams)) {
            ContextCollector collector = featurerService.getFeaturer(group.getFeaturerId(), ContextCollector.class);
            ContextCollectorBatch batch = new ContextCollectorBatch(domainId).add(rootTwin);
            collector.collectDataBatch(batch, group.getParams());
            Map<String, String> collected = batch.getContext(rootTwin);
            if (collected == null) {
                continue;
            }
            for (ContextCollectorEntity config : group.getItems()) {
                result.computeIfAbsent(config.getContextId(), _ -> new HashMap<>()).putAll(collected);
            }
        }
        // register i18n placeholder ids so I18nResponseBodyAdvice bulk-loads their translations
        // (placeholders are materialized by I18nValueSerializer at response write, in the request locale)
        for (Map<String, String> context : result.values()) {
            for (String value : context.values()) {
                if (ContextCollectorBatch.isI18nPlaceholder(value)) {
                    I18nCacheHolder.addId(ContextCollectorBatch.i18nIdOfPlaceholder(value));
                }
            }
        }
        return result;
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
            Map<String, String> collected = batch.getContext(history);
            if (collected != null) {
                context.putAll(collected);
            }
        }
        return context;
    }

    /**
     * Resolves the chunk's i18n in the RECIPIENTS' locales: recipient locales are bulk-loaded once
     * (one query), then every i18n id collected in the chunk is bulk-translated once per distinct
     * locale (groups of one chunk usually share the same i18n ids, e.g. twin class names). A chunk
     * with no i18n skips this entirely — no locale is loaded, so all recipients land in one
     * null-locale group at event build.
     */
    private void resolveI18n(HistoryNotificationChunk chunk, Collection<ContextCollectorBatch> batches) throws ServiceException {
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
     * Materializes a context template for one locale: every {@code #i18n=<uuid>} placeholder value is
     * replaced with its translation for the locale (empty string on a missing translation or a null
     * locale — parity with the old per-id translateToLocale, the raw placeholder never leaks).
     * Non-placeholder values pass through unchanged.
     */
    public Map<String, String> materializeContext(Map<String, String> template, HistoryNotificationChunk chunk, Locale locale) {
        Map<UUID, String> translations = locale != null ? chunk.getI18nTranslationsByLocale().get(locale) : null;
        Map<String, String> context = new HashMap<>(template);
        for (var entry : context.entrySet()) {
            if (!ContextCollectorBatch.isI18nPlaceholder(entry.getValue())) {
                continue;
            }
            UUID i18nId = ContextCollectorBatch.i18nIdOfPlaceholder(entry.getValue());
            String translation = translations != null ? translations.get(i18nId) : null;
            entry.setValue(translation != null ? translation : "");
        }
        return context;
    }
}
