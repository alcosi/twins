package org.twins.core.featurer.twin.executor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.pagination.PaginationResult;
import org.cambium.common.pagination.SimplePagination;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamBoolean;
import org.cambium.featurer.params.FeaturerParamDouble;
import org.cambium.featurer.params.FeaturerParamInt;
import org.cambium.featurer.params.FeaturerParamUUID;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.twins.core.dao.search.TwinSearchEntity;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.domain.ApiUser;
import org.twins.core.domain.TwinFieldClause;
import org.twins.core.domain.TwinFieldFilter;
import org.twins.core.domain.search.BasicSearch;
import org.twins.core.domain.search.ConcatenatedPageWindow;
import org.twins.core.domain.search.ConcatenatedPageWindow.Slice;
import org.twins.core.domain.search.TwinFieldValueSearchNumeric;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.fieldtyper.FieldTyper;
import org.twins.core.featurer.params.FeaturerParamUUIDListTwinsLinkId;
import org.twins.core.featurer.params.FeaturerParamUUIDListTwinsTwinSearchId;
import org.twins.core.featurer.params.FeaturerParamUUIDTwinsTwinClassFieldId;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.twin.TwinSearchService;
import org.twins.core.service.twinclassfield.TwinClassFieldService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Executes a saved search in two sequential steps. First the source search collects the source
 * twins (e.g. SUPPLY_PORTION assigned to the current user), then their ids are injected into each
 * child search as a link restriction: every child returns twins linked to the source twins over
 * the child's own link (e.g. portion -> task). The page is a window of the children concatenation
 * (see {@link ConcatenatedPageWindow}), so child logic must be pairwise disjoint — a twin matched
 * by two children is returned twice. Within a child the query runs with DISTINCT (param
 * {@code distinct}), so a twin linked from several source twins is returned once.
 * <p>
 * Both the source and the children are executed as plain predicate searches
 * ({@link TwinSearchService#toBasicSearch}); their own executors are ignored, so neither can be a
 * concatenation itself. An optional numeric filter ({@code sourceTwinClassFieldId} +
 * {@code sourceFieldMoreThen}, e.g. usefulQuantity &gt; 0) is injected into the source query as a
 * fieldsFilter clause. The child {@link BasicSearch}es are built once per execute/count call and
 * shared by the count phase and the page slices; the slices are fetched without the extra count
 * query Spring Data runs for {@link org.springframework.data.domain.Page}, because the totals are
 * already known. The built child queries and their counts are kept in a short-lived
 * micro-cache (see {@link #EXECUTION_CACHE_TTL_MS}), keyed by the search config, the caller's
 * domain/user and the named params — the frontend typically calls search and then count as two
 * REST requests for the same screen, and without the cache the count call would re-run the source
 * query and every child count all over again. Narrowed searches and oversized executions
 * ({@link #CACHEABLE_SOURCE_IDS_LIMIT} source twins) bypass the cache.
 */
@Component
@RequiredArgsConstructor
@Slf4j
@Lazy
@Featurer(id = FeaturerTwins.ID_5703,
        name = "Sequential source-to-linked search",
        description = "Runs the source saved search, then children as searches of twins linked from the source twins")
public class TwinSearchExecutorSequential extends TwinSearchExecutor {

    @FeaturerParam(name = "Source search id",
            description = "Saved search id collecting the source twins (e.g. user supply portions)",
            order = 1)
    public static final FeaturerParamUUID sourceTwinSearchId = new FeaturerParamUUID("sourceTwinSearchId");

    @FeaturerParam(name = "Source field id",
            description = "Optional twin class field id (together with Source field more then) narrowed as a numeric fieldsFilter clause on the source search, e.g. usefulQuantity",
            order = 2, optional = true)
    public static final FeaturerParamUUIDTwinsTwinClassFieldId sourceTwinClassFieldId = new FeaturerParamUUIDTwinsTwinClassFieldId("sourceTwinClassFieldId");

    @FeaturerParam(name = "Source field more then",
            description = "Optional numeric lower bound for Source field id (strictly greater); both params must be set together",
            order = 3, optional = true)
    public static final FeaturerParamDouble sourceFieldMoreThen = new FeaturerParamDouble("sourceFieldMoreThen");

    @FeaturerParam(name = "Source twins limit",
            description = "Max source twins to resolve; overflow is warned and truncated. Keep moderate: the source ids are embedded as an IN list into every child query, and oversized executions are not cached",
            order = 4, optional = true, defaultValue = "1000")
    public static final FeaturerParamInt sourceTwinsLimit = new FeaturerParamInt("sourceTwinsLimit");

    @FeaturerParam(name = "Child searches",
            description = "Ordered saved search ids, one per location kind. Child logic must not overlap, otherwise the same twin is returned more than once",
            order = 5)
    public static final FeaturerParamUUIDListTwinsTwinSearchId childTwinSearchIds = new FeaturerParamUUIDListTwinsTwinSearchId("childTwinSearchIds");

    @FeaturerParam(name = "Child link ids",
            description = "Link id per child search, same order and count as childTwinSearchIds: child twins are linked to the source twins over this link",
            order = 6)
    public static final FeaturerParamUUIDListTwinsLinkId childLinkIds = new FeaturerParamUUIDListTwinsLinkId("childLinkIds");

    @FeaturerParam(name = "Source is link src",
            description = "true: source twins are the link SRC (children are link DST, e.g. portion -> task); false: source twins are the link DST",
            order = 7, optional = true, defaultValue = "true")
    public static final FeaturerParamBoolean sourceIsSrc = new FeaturerParamBoolean("sourceIsSrc");

    @FeaturerParam(name = "Distinct",
            description = "Run each child query with DISTINCT so a twin linked from several source twins is returned once",
            order = 8, optional = true, defaultValue = "true")
    public static final FeaturerParamBoolean distinct = new FeaturerParamBoolean("distinct");

    @Lazy
    private final TwinSearchService twinSearchService;
    @Lazy
    private final TwinClassFieldService twinClassFieldService;
    @Lazy
    private final AuthService authService;

    /**
     * Execution data shared between the execute and count calls of the same screen. The frontend
     * calls search and then count as separate REST requests, and a fresh resolve would re-run the
     * source query and every child count; the TTL keeps a repeated identical call cheap while
     * bounding staleness. Narrowed searches bypass the cache ({@link #executionCacheKey}), and so
     * do oversized executions ({@link #CACHEABLE_SOURCE_IDS_LIMIT}).
     */
    private static final ConcurrentHashMap<String, CachedExecution> EXECUTION_CACHE = new ConcurrentHashMap<>();
    private static final long EXECUTION_CACHE_TTL_MS = 5_000;
    private static final int EXECUTION_CACHE_MAX_ENTRIES = 256;
    /**
     * Executions over more source twins than this are resolved live, bypassing the cache: every
     * child BasicSearch holds its own copy of the source id set (~64B per id per child), so
     * caching big executions would multiply that by the cache capacity (docs/backlog/033, finding 2).
     */
    private static final int CACHEABLE_SOURCE_IDS_LIMIT = 1000;

    @Override
    protected PaginationResult<TwinEntity> doExecute(Properties properties, TwinSearchEntity search, Map<String, String> namedParams, BasicSearch narrow, SimplePagination pagination, Set<UUID> stack) throws ServiceException {
        Execution execution = resolve(properties, search, namedParams, narrow);
        if (execution == null) {
            PaginationResult<TwinEntity> empty = new PaginationResult<TwinEntity>()
                    .setList(Collections.emptyList())
                    .setTotal(0);
            empty.setOffset(pagination.getOffset());
            empty.setLimit(pagination.getLimit());
            return empty;
        }
        List<TwinEntity> rows = new ArrayList<>();
        for (Slice slice : ConcatenatedPageWindow.slices(execution.counts(), pagination.getOffset(), pagination.getLimit())) {
            BasicSearch childSearch = execution.childSearches().get(slice.index());
            SimplePagination childPagination = new SimplePagination().setOffset(slice.offset()).setLimit(slice.limit());
            rows.addAll(twinSearchService.findTwinsUnalignedNoCount(childSearch, childPagination));
        }
        PaginationResult<TwinEntity> result = new PaginationResult<TwinEntity>()
                .setList(rows)
                .setTotal(ConcatenatedPageWindow.total(execution.counts()));
        result.setOffset(pagination.getOffset());
        result.setLimit(pagination.getLimit());
        return result;
    }

    @Override
    protected long doCount(Properties properties, TwinSearchEntity search, Map<String, String> namedParams, BasicSearch narrow, Set<UUID> stack) throws ServiceException {
        Execution execution = resolve(properties, search, namedParams, narrow);
        return execution == null ? 0 : ConcatenatedPageWindow.total(execution.counts());
    }

    private record Children(List<TwinSearchEntity> searches, List<UUID> linkIds) {
    }

    /**
     * Everything the executor needs after the source resolution: the built child queries and their
     * counts, shared by the page slices and by the following count call (via the micro-cache).
     */
    private record Execution(List<BasicSearch> childSearches, long[] counts) {
    }

    private record CachedExecution(Execution execution, long cachedAtMs) {
        boolean expired() {
            return System.currentTimeMillis() - cachedAtMs > EXECUTION_CACHE_TTL_MS;
        }
    }

    /**
     * Resolves the source twins, builds every child query and counts them — once, so the execute
     * and count phases of one screen share a single source query, a single config load and a
     * single count per child. Returns {@code null} when the source search matched no twins.
     */
    private Execution resolve(Properties properties, TwinSearchEntity search, Map<String, String> namedParams, BasicSearch narrow) throws ServiceException {
        String cacheKey = executionCacheKey(properties, search, namedParams, narrow);
        CachedExecution cached = cacheKey == null ? null : EXECUTION_CACHE.get(cacheKey);
        if (cached != null && cached.expired()) {
            EXECUTION_CACHE.remove(cacheKey, cached);
            cached = null;
        }
        if (cached != null)
            return cached.execution();
        Children children = children(properties);
        Set<UUID> sourceTwinIds = sourceTwinIds(properties, namedParams, narrow);
        if (sourceTwinIds.isEmpty())
            return null;
        // oversized executions stay uncached: the id-set copies dominate the entry footprint
        boolean cacheable = cacheKey != null && sourceTwinIds.size() <= CACHEABLE_SOURCE_IDS_LIMIT;
        long[] counts = new long[children.searches().size()];
        List<BasicSearch> childSearches = new ArrayList<>(children.searches().size());
        for (int i = 0; i < children.searches().size(); i++) {
            BasicSearch childSearch = childSearch(children.searches().get(i), children.linkIds().get(i), properties, sourceTwinIds, namedParams, narrow);
            childSearches.add(childSearch);
            counts[i] = twinSearchService.count(childSearch);
        }
        Execution execution = new Execution(List.copyOf(childSearches), counts);
        if (cacheable)
            cachePut(cacheKey, execution);
        return execution;
    }

    /**
     * Cache key for one logical execution: the caller's domain/user, the full executor config and
     * the named params. Returns {@code null} when the execution must not be cached — narrowed
     * searches, because {@link BasicSearch}'s lombok equals/hashCode covers only its own fields,
     * so a key over the narrow could collide.
     */
    private String executionCacheKey(Properties properties, TwinSearchEntity search, Map<String, String> namedParams, BasicSearch narrow) throws ServiceException {
        if (narrow != null)
            return null;
        ApiUser apiUser = authService.getApiUser();
        return apiUser.getDomainId()
                + "|" + apiUser.getUserId()
                + "|search=" + search.getId()
                + "|source=" + sourceTwinSearchId.extract(properties)
                + "|children=" + childTwinSearchIds.extract(properties)
                + "|links=" + childLinkIds.extract(properties)
                + "|field=" + sourceTwinClassFieldId.extract(properties)
                + "|moreThen=" + sourceFieldMoreThen.extract(properties)
                + "|limit=" + sourceTwinsLimit.extract(properties)
                + "|src=" + sourceIsSrc.extract(properties)
                + "|distinct=" + distinct.extract(properties)
                + "|params=" + namedParams;
    }

    private void cachePut(String cacheKey, Execution execution) {
        while (EXECUTION_CACHE.size() >= EXECUTION_CACHE_MAX_ENTRIES) {
            String eldestKey = null;
            long eldestMs = Long.MAX_VALUE;
            for (Map.Entry<String, CachedExecution> entry : EXECUTION_CACHE.entrySet()) {
                if (entry.getValue().cachedAtMs() < eldestMs) {
                    eldestMs = entry.getValue().cachedAtMs();
                    eldestKey = entry.getKey();
                }
            }
            if (eldestKey == null)
                EXECUTION_CACHE.clear();
            else
                EXECUTION_CACHE.remove(eldestKey);
        }
        EXECUTION_CACHE.put(cacheKey, new CachedExecution(execution, System.currentTimeMillis()));
    }

    private Children children(Properties properties) throws ServiceException {
        List<UUID> ids = childTwinSearchIds.extract(properties);
        List<UUID> linkIds = childLinkIds.extract(properties);
        if (ids.isEmpty())
            throw new ServiceException(ErrorCodeTwins.TWIN_SEARCH_CONFIG_INCORRECT, "sequential search has no child searches");
        if (ids.size() != linkIds.size())
            throw new ServiceException(ErrorCodeTwins.TWIN_SEARCH_CONFIG_INCORRECT, "each child search needs exactly one child link id");
        return new Children(twinSearchService.loadSearchesInOrder(ids), linkIds);
    }

    /**
     * Resolves the source twin ids once per execute/count call, so all children share one source query.
     */
    private Set<UUID> sourceTwinIds(Properties properties, Map<String, String> namedParams, BasicSearch narrow) throws ServiceException {
        UUID sourceTwinSearchId = TwinSearchExecutorSequential.sourceTwinSearchId.extract(properties);
        TwinSearchEntity source = twinSearchService.loadSearchesInOrder(List.of(sourceTwinSearchId)).getFirst();
        int limit = sourceTwinsLimit.extract(properties);
        if (limit < 1)
            throw new ServiceException(ErrorCodeTwins.TWIN_SEARCH_CONFIG_INCORRECT, "sourceTwinsLimit must be positive");
        BasicSearch sourceSearch = twinSearchService.toBasicSearch(source, namedParams, narrow);
        injectSourceFieldFilter(properties, sourceSearch);
        // only the ids are consumed - no point preloading the EAGER refs of the source page
        PaginationResult<TwinEntity> sourceResult = twinSearchService.findTwinsUnaligned(sourceSearch, new SimplePagination().setOffset(0).setLimit(limit), false);
        if (sourceResult.getTotal() > limit)
            log.warn("sequential search source[{}] returned {} twins, capped to sourceTwinsLimit[{}] - locations beyond the cap are missed",
                    sourceTwinSearchId, sourceResult.getTotal(), limit);
        return sourceResult.getList().stream().map(TwinEntity::getId).collect(Collectors.toSet());
    }

    private void injectSourceFieldFilter(Properties properties, BasicSearch sourceSearch) throws ServiceException {
        UUID fieldId = sourceTwinClassFieldId.extract(properties);
        Double moreThen = sourceFieldMoreThen.extract(properties);
        if (fieldId == null && moreThen == null)
            return;
        if (fieldId == null || moreThen == null)
            throw new ServiceException(ErrorCodeTwins.TWIN_SEARCH_CONFIG_INCORRECT, "sourceTwinClassFieldId and sourceFieldMoreThen must be set together");
        TwinFieldValueSearchNumeric fieldSearch = new TwinFieldValueSearchNumeric().setMoreThen(moreThen);
        fieldSearch.setTwinClassFieldEntity(twinClassFieldService.findEntitySafe(fieldId));
        fieldSearch.setFieldTyper(featurerService.getFeaturer(fieldSearch.getTwinClassFieldEntity().getFieldTyperFeaturerId(), FieldTyper.class));
        sourceSearch.setFieldsFilter(new TwinFieldFilter().addClause(new TwinFieldClause().addCondition(fieldSearch)));
    }

    /**
     * Builds the child query from its predicates and injects the link restriction over the source twin ids.
     */
    private BasicSearch childSearch(TwinSearchEntity child, UUID linkId, Properties properties, Set<UUID> sourceTwinIds, Map<String, String> namedParams, BasicSearch narrow) throws ServiceException {
        BasicSearch basicSearch = twinSearchService.toBasicSearch(child, namedParams, narrow);
        if (Boolean.TRUE.equals(sourceIsSrc.extract(properties)))
            basicSearch.addLinkSrcTwinsId(linkId, sourceTwinIds, false, true);
        else
            basicSearch.addLinkDstTwinsId(linkId, sourceTwinIds, false, true);
        basicSearch.setDistinct(Boolean.TRUE.equals(distinct.extract(properties)));
        return basicSearch;
    }
}
