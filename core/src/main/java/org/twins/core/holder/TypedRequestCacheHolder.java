package org.twins.core.holder;

import java.util.HashMap;
import java.util.Map;

/**
 * Request-scoped side cache keyed by (Class namespace, string cacheId) — for per-entity data that must
 * NOT live on the entity itself: a {@code @Transient} field is the wrong place for request-scoped
 * values (the same entity instance can outlive the request via JPA session/caches, and single-entity
 * code paths never see batch-preloaded fields).
 * <p>
 * The Class key is the NAMESPACE of what the cache is about (usually an entity class — the cached
 * values themselves may be of any type: converted DTOs, preloaded entities, per-entity collected
 * data); the string cacheId is the entry key inside the namespace (usually a feature prefix + entity
 * id). The value type is trusted to the caller.
 * <p>
 * Used both from the mapper layer ({@code MapperContext.getFromCache/putToCache} delegate here, so
 * batch-preloaded data stays visible to single-entity map() calls and to forked/postponed
 * conversions) and directly from services — no MapperContext passing needed.
 * <p>
 * Same threading/cleanup contract as {@link EntityRequestCacheHolder}: a ThreadLocal, cleared by
 * {@link org.twins.core.config.filter.I18nCacheCleanupFilter} for web requests and by
 * {@code Scheduler.getRunnableForScheduling}'s {@code finally} for scheduler tasks; any other
 * non-web entrypoint ({@code @Async} etc.) must wrap its body in {@code try/finally clear()}.
 */
public class TypedRequestCacheHolder {

    private static final ThreadLocal<Map<Class<?>, Map<String, Object>>> CACHE = ThreadLocal.withInitial(HashMap::new);

    @SuppressWarnings("unchecked")
    public static <S> S get(Class<?> cacheType, String cacheId) {
        if (cacheId == null) {
            return null;
        }
        Map<String, Object> cache = CACHE.get().get(cacheType);
        return cache == null ? null : (S) cache.get(cacheId);
    }

    public static void put(Class<?> cacheType, String cacheId, Object obj) {
        if (cacheId == null || obj == null) {
            return;
        }
        CACHE.get().computeIfAbsent(cacheType, key -> new HashMap<>()).put(cacheId, obj);
    }

    public static void clear() {
        CACHE.remove();
    }
}
