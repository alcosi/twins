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
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.featurer.context.ContextCollector;
import org.twins.core.featurer.context.ContextCollectorBatch;
import org.twins.core.holder.I18nCacheHolder;
import org.twins.core.service.auth.AuthService;

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
}
