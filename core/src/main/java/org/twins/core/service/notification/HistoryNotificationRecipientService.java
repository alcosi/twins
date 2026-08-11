package org.twins.core.service.notification;

import io.github.breninsul.logging.aspect.JavaLoggingLevel;
import io.github.breninsul.logging.aspect.annotation.LogExecutionTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.kit.Kit;
import org.cambium.common.util.ChangesHelper;
import org.cambium.common.util.ChangesHelperMulti;
import org.cambium.common.util.CollectionUtils;
import org.cambium.featurer.FeaturerService;
import org.cambium.service.EntitySecureFindServiceImpl;
import org.cambium.service.EntitySmartService;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.twins.core.dao.history.HistoryEntity;
import org.twins.core.dao.notification.*;
import org.twins.core.domain.notification.HistoryNotificationRecipientCreate;
import org.twins.core.domain.notification.HistoryNotificationRecipientUpdate;
import org.twins.core.enums.i18n.I18nType;
import org.twins.core.featurer.notificator.recipient.RecipientResolver;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.i18n.I18nService;
import org.twins.core.service.user.UserService;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
@LogExecutionTime(logPrefix = "LONG EXECUTION TIME:", logIfTookMoreThenMs = 2 * 1000, level = JavaLoggingLevel.WARNING)
@Slf4j
@Lazy
@RequiredArgsConstructor
public class HistoryNotificationRecipientService extends EntitySecureFindServiceImpl<HistoryNotificationRecipientEntity> {
    private final HistoryNotificationRecipientRepository repository;
    private final I18nService i18nService;
    private final AuthService authService;
    private final UserService userService;
    private final HistoryNotificationRecipientCollectorService historyNotificationRecipientCollectorService;
    private final FeaturerService featurerService;

    @Override
    public CrudRepository<HistoryNotificationRecipientEntity, UUID> entityRepository() {
        return repository;
    }

    @Override
    public Function<HistoryNotificationRecipientEntity, UUID> entityGetIdFunction() {
        return HistoryNotificationRecipientEntity::getId;
    }

    @Override
    public boolean isEntityReadDenied(HistoryNotificationRecipientEntity entity, EntitySmartService.ReadPermissionCheckMode readPermissionCheckMode) throws ServiceException {
        return false;
    }

    @Override
    public boolean validateEntity(HistoryNotificationRecipientEntity entity, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
        return true;
    }

    @Transactional(rollbackFor = Throwable.class)
    public List<HistoryNotificationRecipientEntity> createHistoryNotificationRecipients(List<HistoryNotificationRecipientCreate> recipients) throws ServiceException {
        if (recipients == null || recipients.isEmpty()) {
            return Collections.emptyList();
        }

        List<HistoryNotificationRecipientEntity> recipientsToSave = new ArrayList<>();

        for (HistoryNotificationRecipientCreate recipient : recipients) {
            HistoryNotificationRecipientEntity recipientEntity = new HistoryNotificationRecipientEntity()
                    .setNameI18nId(i18nService.createI18nAndTranslations(I18nType.HISTORY_NOTIFICATION_RECIPIENT_NAME, recipient.getNameI18n()).getId())
                    .setDescriptionI18nId(i18nService.createI18nAndTranslations(I18nType.HISTORY_NOTIFICATION_RECIPIENT_DESCRIPTION, recipient.getDescriptionI18n()).getId())
                    .setCreatedAt(Timestamp.from(Instant.now()))
                    .setCreatedByUserId(authService.getApiUser().getUserId())
                    .setDomainId(authService.getApiUser().getDomainId());

            recipientsToSave.add(recipientEntity);
        }

        return StreamSupport.stream(saveSafe(recipientsToSave).spliterator(), false).toList();
    }

    @Transactional(rollbackFor = Throwable.class)
    public List<HistoryNotificationRecipientEntity> updateHistoryNotificationRecipients(List<HistoryNotificationRecipientUpdate> recipients) throws ServiceException {
        if (recipients == null || recipients.isEmpty()) {
            return Collections.emptyList();
        }

        ChangesHelperMulti<HistoryNotificationRecipientEntity> changes = new ChangesHelperMulti<>();
        List<HistoryNotificationRecipientEntity> allEntities = new ArrayList<>(recipients.size());

        Kit<HistoryNotificationRecipientEntity, UUID> entitiesKit = findEntitiesSafe(recipients.stream().map(HistoryNotificationRecipientUpdate::getId).toList());

        for (HistoryNotificationRecipientUpdate recipient : recipients) {
            HistoryNotificationRecipientEntity entity = entitiesKit.get(recipient.getId());
            allEntities.add(entity);

            ChangesHelper changesHelper = new ChangesHelper();
            i18nService.updateI18nFieldForEntity(recipient.getNameI18n(), I18nType.HISTORY_NOTIFICATION_RECIPIENT_NAME, entity,
                    HistoryNotificationRecipientEntity::getNameI18nId, HistoryNotificationRecipientEntity::setNameI18nId,
                    HistoryNotificationRecipientEntity.Fields.nameI18nId, changesHelper);
            i18nService.updateI18nFieldForEntity(recipient.getDescriptionI18n(), I18nType.HISTORY_NOTIFICATION_RECIPIENT_DESCRIPTION, entity,
                    HistoryNotificationRecipientEntity::getDescriptionI18nId, HistoryNotificationRecipientEntity::setDescriptionI18nId,
                    HistoryNotificationRecipientEntity.Fields.descriptionI18nId, changesHelper);

            changes.add(entity, changesHelper);
        }

        updateSafe(changes);

        return allEntities;
    }

    public void loadCreatedByUser(HistoryNotificationRecipientEntity entity) throws ServiceException {
        loadCreatedByUser(List.of(entity));
    }

    public void loadCreatedByUser(Collection<HistoryNotificationRecipientEntity> entities) throws ServiceException {
        userService.load(entities,
                HistoryNotificationRecipientEntity::getCreatedByUserId,
                HistoryNotificationRecipientEntity::getCreatedByUser,
                HistoryNotificationRecipientEntity::setCreatedByUser);
    }

    public void loadCollectors(Collection<HistoryNotificationRecipientEntity> entities) {
        loadKit(
                entities,
                HistoryNotificationRecipientEntity::getId,
                HistoryNotificationRecipientEntity::getCollectors,
                HistoryNotificationRecipientEntity::setCollectors,
                historyNotificationRecipientCollectorService::findByHistoryNotificationRecipientIdIn,
                HistoryNotificationRecipientCollectorEntity::getId,
                HistoryNotificationRecipientCollectorEntity::getHistoryNotificationRecipientId,
                HistoryNotificationRecipientCollectorEntity::setHistoryNotificationRecipient);
    }

    public Set<UUID> recipientResolve(HistoryNotificationRecipientEntity recipient, HistoryEntity history) throws ServiceException {
        var partitioned = recipient.getCollectors().getList().stream()
                .collect(Collectors.partitioningBy(HistoryNotificationRecipientCollectorEntity::getExclude));

        var include = resolveRecipient(history, partitioned.get(false));
        if (include.isEmpty()) {
            return include;
        }

        var excludeCollectors = partitioned.get(true);
        if (CollectionUtils.isNotEmpty(excludeCollectors)) {
            var exclude = resolveRecipient(history, excludeCollectors);
            include.removeAll(exclude);
        }

        return include;
    }

    private Set<UUID> resolveRecipient(HistoryEntity history, List<HistoryNotificationRecipientCollectorEntity> collectors) throws ServiceException {
        Set<UUID> result = new HashSet<>();
        Map<HistoryEntity, Set<UUID>> batch = new HashMap<>();
        batch.put(history, result);
        for (HistoryNotificationRecipientCollectorEntity collector : collectors) {
            RecipientResolver resolver = featurerService.getFeaturer(collector.getRecipientResolverFeaturerId(), RecipientResolver.class);
            resolver.resolveBatch(batch, collector.getRecipientResolverParams());
        }
        return result;
    }

    /**
     * Chunk-level batch resolve: one {@link RecipientResolver#resolveBatch} per unique
     * {@code (resolverFeaturerId, canonical params, exclude)} group across the whole chunk, so that
     * {@code beforeResolve} preload fires once per group instead of once per history.
     * <p>Resolver is deterministic by {@code (history, featurerId, params)}, so each group is resolved
     * once for all its histories, then per-(recipient, history) include/exclude is reassembled (exclude
     * is applied per-recipient before any union — see {@link #recipientResolve}) and stored on
     * {@link HistoryNotificationTaskEntity#getResolvedRecipientsByRecipientId()} for processTask.
     */
    public void resolveRecipientsBatch(
            Map<HistoryNotificationTaskEntity, List<HistoryNotificationEntity>> configsByTask) throws ServiceException {
        if (CollectionUtils.isEmpty(configsByTask)) {
            return;
        }
        // Stage 1: unique (featurerId, params, exclude) groups (histories = union across recipients) +
        //   per-recipient precomputed group keys (partitioning + toConfigKey done ONCE per recipient).
        Map<String, ResolverGroup> groups = new HashMap<>();
        Map<UUID, RecipientKeys> keysByRecipient = new HashMap<>();
        for (var entry : configsByTask.entrySet()) {
            HistoryEntity history = entry.getKey().getHistory();
            if (history == null) {
                continue;
            }
            for (HistoryNotificationEntity config : entry.getValue()) {
                HistoryNotificationRecipientEntity recipient = config.getHistoryNotificationRecipient();
                if (recipient == null || recipient.getCollectors() == null) {
                    continue;
                }
                keysByRecipient.computeIfAbsent(recipient.getId(), id -> RecipientKeys.of(recipient));
                collectResolverGroups(groups, recipient, history);
            }
        }

        // Stage 2: one resolveBatch per group → resolverCache (shared accumulator, do not mutate later)
        Map<String, Map<HistoryEntity, Set<UUID>>> resolverCache = new HashMap<>();
        for (var e : groups.entrySet()) {
            ResolverGroup g = e.getValue();
            RecipientResolver resolver = featurerService.getFeaturer(g.featurerId, RecipientResolver.class);
            Map<HistoryEntity, Set<UUID>> accumulator = new HashMap<>();
            for (HistoryEntity history : g.histories) {
                accumulator.put(history, new HashSet<>());
            }
            resolver.resolveBatch(accumulator, g.params);
            resolverCache.put(e.getKey(), accumulator);
        }

        // Stage 3: reassemble per-(recipient, history) from cache → write to task-entity.
        //   Cheap: a few map lookups per (recipient, history), no re-partitioning / re-hashing.
        for (var entry : configsByTask.entrySet()) {
            HistoryNotificationTaskEntity task = entry.getKey();
            HistoryEntity history = task.getHistory();
            Map<UUID, Set<UUID>> byRecipient = new HashMap<>();
            if (history != null) {
                for (HistoryNotificationEntity config : entry.getValue()) {
                    HistoryNotificationRecipientEntity recipient = config.getHistoryNotificationRecipient();
                    if (recipient == null) {
                        continue;
                    }
                    byRecipient.computeIfAbsent(recipient.getId(),
                            id -> resolveFromCache(resolverCache, keysByRecipient.get(id), history));
                }
            }
            task.setResolvedRecipientsByRecipientId(byRecipient);
        }
    }

    private static String groupKey(int featurerId, HashMap<String, String> params, boolean exclude) {
        return FeaturerService.toConfigKey(featurerId, params) + "|" + exclude;
    }

    private void collectResolverGroups(Map<String, ResolverGroup> groups,
                                       HistoryNotificationRecipientEntity recipient, HistoryEntity history) {
        for (HistoryNotificationRecipientCollectorEntity collector : recipient.getCollectors().getList()) {
            int featurerId = collector.getRecipientResolverFeaturerId();
            boolean exclude = Boolean.TRUE.equals(collector.getExclude());
            String key = groupKey(featurerId, collector.getRecipientResolverParams(), exclude);
            groups.computeIfAbsent(key, k -> new ResolverGroup(featurerId, collector.getRecipientResolverParams())).histories.add(history);
        }
    }

    private static Set<UUID> resolveFromCache(Map<String, Map<HistoryEntity, Set<UUID>>> resolverCache,
                                              RecipientKeys keys, HistoryEntity history) {
        if (keys == null) {
            return Set.of();
        }
        Set<UUID> include = unionFromCache(resolverCache, keys.include, history);
        if (include.isEmpty()) {
            return Set.of();
        }
        if (!keys.exclude.isEmpty()) {
            include.removeAll(unionFromCache(resolverCache, keys.exclude, history));
        }
        return include;
    }

    private static Set<UUID> unionFromCache(Map<String, Map<HistoryEntity, Set<UUID>>> resolverCache,
                                            List<String> groupKeys, HistoryEntity history) {
        Set<UUID> result = new HashSet<>();
        for (String groupKey : groupKeys) {
            Map<HistoryEntity, Set<UUID>> accumulator = resolverCache.get(groupKey);
            if (accumulator != null) {
                Set<UUID> resolved = accumulator.get(history);
                if (resolved != null) {
                    result.addAll(resolved);
                }
            }
        }
        return result;
    }

    /** Precomputed per-recipient group keys: partitioning + canonical key done once, reused per history. */
    private static final class RecipientKeys {
        final List<String> include = new ArrayList<>();
        final List<String> exclude = new ArrayList<>();

        static RecipientKeys of(HistoryNotificationRecipientEntity recipient) {
            RecipientKeys keys = new RecipientKeys();
            for (HistoryNotificationRecipientCollectorEntity collector : recipient.getCollectors().getList()) {
                int featurerId = collector.getRecipientResolverFeaturerId();
                boolean excl = Boolean.TRUE.equals(collector.getExclude());
                (excl ? keys.exclude : keys.include)
                        .add(groupKey(featurerId, collector.getRecipientResolverParams(), excl));
            }
            return keys;
        }
    }

    /** Unique resolver group across the chunk: (featurerId, params) + union of histories where it occurs. */
    private static final class ResolverGroup {
        final int featurerId;
        final HashMap<String, String> params;
        final Set<HistoryEntity> histories = new HashSet<>();

        ResolverGroup(int featurerId, HashMap<String, String> params) {
            this.featurerId = featurerId;
            this.params = params;
        }
    }
}
