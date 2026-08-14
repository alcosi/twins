package org.twins.core.service.notification;

import io.github.breninsul.logging.aspect.JavaLoggingLevel;
import io.github.breninsul.logging.aspect.annotation.LogExecutionTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.kit.Kit;
import org.cambium.common.util.ChangesHelper;
import org.cambium.common.util.ChangesHelperMulti;
import org.cambium.featurer.FeaturerGroup;
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
import org.twins.core.featurer.notificator.recipient.RecipientResolveBatch;
import org.twins.core.featurer.notificator.recipient.RecipientResolver;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.i18n.I18nService;
import org.twins.core.service.user.UserService;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.*;
import java.util.function.Function;
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


    /**
     * Chunk-level batch resolve: one {@link RecipientResolver#resolveBatch} per unique
     * {@code (resolverFeaturerId, canonical params)} group across the whole chunk, so that
     * {@code beforeResolve} preload fires once per group instead of once per history. Resolver is
     * deterministic by {@code (history, featurerId, params)} (independent of include/exclude), so each
     * group is resolved once for all its histories, then per-(recipient, history) include/exclude is
     * reassembled and stored on {@link HistoryNotificationTaskEntity#getResolvedRecipientsByRecipientId()} for processTask.
     */
    public void resolveRecipientsBatch(HistoryNotificationChunk chunk) throws ServiceException {
        if (chunk == null || chunk.getTasksByConfig().isEmpty()) {
            return;
        }
        UUID chunkDomainId = chunk.getDomainId();
        // Stage 1: per-recipient precomputed keys (partitioning + canonical key done ONCE per recipient)
        //   + (collector, history) pairs accumulated straight into (featurerId, params) groups
        //   (config-centric via tasksByConfig: recipient/collectors are fixed per config).
        Map<UUID, RecipientKeys> keysByRecipient = new HashMap<>();
        var resolverGroups = FeaturerGroup.builder(
                HistoryNotificationRecipientCollectorEntity::getRecipientResolverFeaturerId,
                HistoryNotificationRecipientCollectorEntity::getRecipientResolverParams,
                HistoryEntity.class);
        for (var e : chunk.getTasksByConfig().entrySet()) {
            HistoryNotificationEntity config = e.getKey();
            HistoryNotificationRecipientEntity recipient = config.getHistoryNotificationRecipient();
            if (recipient == null || recipient.getCollectors() == null) {
                continue;
            }
            keysByRecipient.computeIfAbsent(recipient.getId(), _ -> RecipientKeys.of(recipient));
            for (HistoryNotificationRecipientCollectorEntity collector : recipient.getCollectors().getList()) {
                for (HistoryNotificationTaskEntity task : e.getValue()) {
                    resolverGroups.add(collector, task.getHistory());
                }
            }
        }

        // Stage 2: one resolveBatch per (featurerId, params) group → resolverCache keyed by toConfigKey
        //   (independent of exclude — same result either way). businessAccountIds are derived per group
        //   by the context (lazy).
        Map<String, RecipientResolveBatch> resolverCache = new HashMap<>();
        for (var group : resolverGroups.build()) {
            RecipientResolver resolver = featurerService.getFeaturer(group.getFeaturerId(), RecipientResolver.class);
            RecipientResolveBatch resolveBatch = new RecipientResolveBatch(chunkDomainId);
            for (HistoryEntity history : group.getItems()) {
                resolveBatch.add(history);
            }
            resolver.resolveBatch(resolveBatch, group.getParams());
            resolverCache.put(group.getConfigKey(), resolveBatch);
        }

        // Stage 3: reassemble per-(recipient, history) from cache → write to task-entity.
        //   Cheap: a few map lookups per (recipient, history), no re-partitioning / re-hashing.
        for (var e : chunk.getConfigsByTask().entrySet()) {
            HistoryNotificationTaskEntity task = e.getKey();
            HistoryEntity history = task.getHistory();
            Map<UUID, Set<UUID>> byRecipient = new HashMap<>();
            for (HistoryNotificationEntity config : e.getValue()) {
                byRecipient.computeIfAbsent(config.getHistoryNotificationRecipientId(),
                        id -> resolveFromCache(resolverCache, keysByRecipient.get(id), history));
            }
            task.setResolvedRecipientsByRecipientId(byRecipient);
        }
    }

    private static Set<UUID> resolveFromCache(Map<String, RecipientResolveBatch> resolverCache,
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

    private static Set<UUID> unionFromCache(Map<String, RecipientResolveBatch> resolverCache,
                                            List<String> groupKeys, HistoryEntity history) {
        Set<UUID> result = new HashSet<>();
        for (String groupKey : groupKeys) {
            var context = resolverCache.get(groupKey);
            if (context != null) {
                Set<UUID> resolved = context.getRecipientIdsByHistory().get(history);
                if (resolved != null) {
                    result.addAll(resolved);
                }
            }
        }
        return result;
    }

    /**
     * Precomputed per-recipient group keys: partitioning + canonical key done once, reused per history.
     */
    private static final class RecipientKeys {
        final List<String> include = new ArrayList<>();
        final List<String> exclude = new ArrayList<>();

        static RecipientKeys of(HistoryNotificationRecipientEntity recipient) {
            RecipientKeys keys = new RecipientKeys();
            for (HistoryNotificationRecipientCollectorEntity collector : recipient.getCollectors().getList()) {
                String key = FeaturerService.toConfigKey(collector.getRecipientResolverFeaturerId(), collector.getRecipientResolverParams());
                if (Boolean.TRUE.equals(collector.getExclude()))
                    keys.exclude.add(key);
                else
                    keys.include.add(key);
            }
            return keys;
        }
    }

}
