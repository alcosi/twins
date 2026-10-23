package org.twins.core.holder;

import java.util.HashMap;
import java.util.Map;

/**
 * Request-scoped caches on a plain ThreadLocal — the single store for per-request per-entity data, in
 * web requests and out of them (schedulers, {@code @Async}). {@code RequestContextHolder} is
 * deliberately NOT used: it is ThreadLocal-based itself, and a dual store (request attributes in web,
 * ThreadLocal elsewhere) only made the cache behavior scope-dependent.
 * <p>
 * Two caches:
 * <ul>
 *   <li>{@link #get(String)}/{@link #put(String, Object)} — flat per-ENTITY cache; the key is already
 *       class-prefixed by the caller (see {@code EntitySecureFindServiceImpl.entityCacheKey}), so
 *       entities of every type share one map without collisions;</li>
 *   <li>{@link #get(Class, String)}/{@link #put(Class, String, Object)} — typed side cache for
 *       per-entity data of ANY type (converted DTOs, preloaded entities, per-entity collected data)
 *       that must not live on the entity itself via a {@code @Transient} field (the instance can
 *       outlive the request via JPA session/caches, and single-entity paths never see batch-preloaded
 *       fields). The Class key is the NAMESPACE of what the cache is about (usually an entity class);
 *       the cacheId is the entry key inside it (usually a feature prefix + entity id). The value type
 *       is trusted to the caller.</li>
 * </ul>
 * <p>
 * Cleanup contract (pooled threads must never serve stale entries):
 * <ul>
 *   <li>web requests — {@link org.twins.core.config.filter.I18nCacheCleanupFilter} clears BEFORE and
 *       AFTER the chain (clear-before drops leftovers of a previous ERROR dispatch on the same pooled
 *       worker — OncePerRequestFilter does not re-run on error dispatches);</li>
 *   <li>schedulers — {@code Scheduler.getRunnableForScheduling} wraps {@code processTask} in
 *       {@code try/finally clear()};</li>
 *   <li>{@code @Async} and any other non-web entrypoint — must wrap its body the same way.</li>
 * </ul>
 */
public class EntityRequestCacheHolder {

    private static final ThreadLocal<Map<String, Object>> ENTITY_CACHE = ThreadLocal.withInitial(HashMap::new);
    private static final ThreadLocal<Map<Class<?>, Map<String, Object>>> TYPED_CACHE = ThreadLocal.withInitial(HashMap::new);

    @SuppressWarnings("unchecked")
    public static <T> T get(String key) {
        return (T) ENTITY_CACHE.get().get(key);
    }

    public static void put(String key, Object entity) {
        ENTITY_CACHE.get().put(key, entity);
    }

    @SuppressWarnings("unchecked")
    public static <S> S get(Class<?> cacheType, String cacheId) {
        if (cacheId == null) {
            return null;
        }
        Map<String, Object> cache = TYPED_CACHE.get().get(cacheType);
        return cache == null ? null : (S) cache.get(cacheId);
    }

    public static void put(Class<?> cacheType, String cacheId, Object obj) {
        if (cacheId == null || obj == null) {
            return;
        }
        TYPED_CACHE.get().computeIfAbsent(cacheType, key -> new HashMap<>()).put(cacheId, obj);
    }

    public static void clear() {
        ENTITY_CACHE.remove();
        TYPED_CACHE.remove();
    }
}
