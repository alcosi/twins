package org.twins.core.holder;

import java.util.HashMap;
import java.util.Map;

/**
 * Thread-local fallback for the per-entity REQUEST cache ({@link org.cambium.service.EntitySecureFindServiceImpl}
 * {@code getCachedEntity}/{@code putCachedEntity}) when no web request scope is available — i.e. inside
 * schedulers (run on virtual threads via {@code virtualThreadTaskScheduler}) and {@code @Async} methods,
 * where {@code RequestContextHolder.getRequestAttributes()} is null.
 * <p>
 * The cache key is already class-prefixed by the service (see {@code entityCacheKey}), so a single flat map
 * holds entities of every type without collisions. Virtual threads created by
 * {@code SimpleAsyncTaskScheduler} are not pooled/reused, so a forgotten cleanup does not leak across tasks
 * the way it would on a pooled platform thread — but every entrypoint must still clear it in {@code finally}
 * to avoid serving stale values from a previous step within the same task.
 * <p>
 * Cleanup points:
 * <ul>
 *   <li>web requests — {@link org.twins.core.config.filter.I18nCacheCleanupFilter} (runs in the same
 *       {@code finally} as {@link I18nCacheHolder#clear()});</li>
 *   <li>schedulers — {@code Scheduler.getRunnableForScheduling} wraps {@code processTask} in
 *       {@code try/finally clear()};</li>
 *   <li>{@code @Async} and any other non-web entrypoint — must wrap their body the same way.</li>
 * </ul>
 */
public class EntityRequestCacheHolder {

    private static final ThreadLocal<Map<String, Object>> CACHE = ThreadLocal.withInitial(HashMap::new);

    @SuppressWarnings("unchecked")
    public static <T> T get(String key) {
        return (T) CACHE.get().get(key);
    }

    public static void put(String key, Object entity) {
        CACHE.get().put(key, entity);
    }

    public static void clear() {
        CACHE.remove();
    }
}
