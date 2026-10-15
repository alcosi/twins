package org.twins.core.featurer.twin.finder;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.util.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.twins.core.domain.ApiUser;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.holder.EntityRequestCacheHolder;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.twin.TwinSearchService;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Shared lookup of twin ids produced by a saved search.
 * Finders that run the same subquery within one request share that lookup. The link id of each
 * search is applied by the finder itself; this lookup only returns the subquery ids.
 * <p>
 * The lookup lives in the request scope: {@link RequestContextHolder} request attributes in web
 * requests, {@link EntityRequestCacheHolder} (thread-local, same cleanup contract) in background
 * processes without a web scope — schedulers, {@code @Async} — the same split as
 * {@link ApiUser}. Nothing survives the request, so every execution sees fresh subquery
 * data; the cost is one subquery run per request (the search and the count REST calls do not
 * share it).
 */
@Slf4j
public abstract class TwinFinderSubquery extends TwinFinder {

    private static final String CACHE_KEY_PREFIX = "TwinFinderSubquery_";

    /**
     * Subquery searches currently being resolved on this thread. The subquery runs through its own
     * executor, whose stack guard starts fresh on every public call — without this chain a search
     * whose finder (directly or through other searches) references itself would recurse forever.
     */
    private static final ThreadLocal<Set<UUID>> RESOLVING_SUBQUERIES = ThreadLocal.withInitial(HashSet::new);

    @Autowired
    TwinSearchService twinSearchService;
    @Autowired
    AuthService authService;

    /**
     * Ids from the saved search, capped to {@code limit}. Repeated calls in the same request
     * return the cached set. Without a caller (no auth) the search runs every time.
     */
    protected Set<UUID> takeSubqueryTwinIds(UUID subQueryTwinSearchId, Map<String, String> namedParams, int limit) throws ServiceException {
        String cacheKey = cacheKey(subQueryTwinSearchId, namedParams);
        if (cacheKey == null)
            return loadSubqueryTwinIds(subQueryTwinSearchId, namedParams, limit);
        Set<UUID> cached = get(cacheKey);
        if (cached != null)
            return cached;
        Set<UUID> ids = loadSubqueryTwinIds(subQueryTwinSearchId, namedParams, limit);
        put(cacheKey, ids);
        return ids;
    }

    private Set<UUID> loadSubqueryTwinIds(UUID subQueryTwinSearchId, Map<String, String> namedParams, int limit) throws ServiceException {
        Set<UUID> resolving = RESOLVING_SUBQUERIES.get();
        if (!resolving.add(subQueryTwinSearchId))
            throw new ServiceException(ErrorCodeTwins.TWIN_SEARCH_CONFIG_INCORRECT,
                    "circular subquery: saved search [" + subQueryTwinSearchId + "] resolves itself through TwinFinderByLinkFromSubquery");
        try {
            return twinSearchService.findTwinIds(subQueryTwinSearchId, namedParams, limit);
        } finally {
            resolving.remove(subQueryTwinSearchId);
        }
    }

    private String cacheKey(UUID subQueryTwinSearchId, Map<String, String> namedParams) throws ServiceException {
        if (authService == null || subQueryTwinSearchId == null)
            return null;
        ApiUser apiUser = authService.getApiUser();
        if (apiUser == null || apiUser.getUserId() == null)
            return null;
        return CACHE_KEY_PREFIX + apiUser.getDomainId()
                + "|" + apiUser.getUserId()
                + "|search=" + subQueryTwinSearchId
                + "|params=" + MapUtils.canonical(namedParams);
    }

    @SuppressWarnings("unchecked")
    private static Set<UUID> get(String key) {
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (requestAttributes != null)
            return (Set<UUID>) requestAttributes.getAttribute(key, RequestAttributes.SCOPE_REQUEST);
        return EntityRequestCacheHolder.get(key);
    }

    private static void put(String key, Set<UUID> ids) {
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (requestAttributes != null)
            requestAttributes.setAttribute(key, ids, RequestAttributes.SCOPE_REQUEST);
        else
            EntityRequestCacheHolder.put(key, ids);
    }
}
