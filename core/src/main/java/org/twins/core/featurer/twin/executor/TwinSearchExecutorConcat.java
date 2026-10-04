package org.twins.core.featurer.twin.executor;

import lombok.RequiredArgsConstructor;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.pagination.PaginationResult;
import org.cambium.common.pagination.SimplePagination;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.twins.core.dao.search.TwinSearchEntity;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.domain.search.BasicSearch;
import org.twins.core.domain.search.ConcatenatedPageWindow;
import org.twins.core.domain.search.ConcatenatedPageWindow.Slice;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.params.FeaturerParamUUIDListTwinsTwinSearchId;
import org.twins.core.service.twin.TwinSearchService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;

/**
 * Executes a saved search as a concatenation of child saved searches.
 * Children run in parameter order, each with its own filter and sort.
 * The page is a window of that concatenation, so the number of children is not fixed.
 * Child searches are executed by their own executor, so a child may itself be a concatenation.
 * Segments are appended, not deduplicated: the same twin returned by two children appears twice.
 */
@Component
@RequiredArgsConstructor
@Featurer(id = FeaturerTwins.ID_5702,
        name = "Concatenate searches",
        description = "Concatenates child saved searches in parameter order. Each child keeps its own filter and sort. Pagination and total apply to the concatenation")
public class TwinSearchExecutorConcat extends TwinSearchExecutor {

    @FeaturerParam(name = "Child searches",
            description = "Ordered saved search ids. The concatenation follows this order",
            order = 1)
    public static final FeaturerParamUUIDListTwinsTwinSearchId childTwinSearchIds = new FeaturerParamUUIDListTwinsTwinSearchId("childTwinSearchIds");

    @Lazy
    private final TwinSearchService twinSearchService;

    @Override
    protected PaginationResult<TwinEntity> doExecute(Properties properties, TwinSearchEntity search, Map<String, String> namedParams, BasicSearch narrow, SimplePagination pagination, Set<UUID> stack) throws ServiceException {
        List<TwinSearchEntity> children = children(properties);
        long[] counts = counts(children, namedParams, narrow, stack);
        List<TwinEntity> rows = new ArrayList<>();
        for (Slice slice : ConcatenatedPageWindow.slices(counts, pagination.getOffset(), pagination.getLimit())) {
            SimplePagination childPagination = new SimplePagination().setOffset(slice.offset()).setLimit(slice.limit());
            rows.addAll(twinSearchService.runSearchExecutor(children.get(slice.index()), namedParams, narrow, childPagination, stack).getList());
        }
        PaginationResult<TwinEntity> result = new PaginationResult<TwinEntity>()
                .setList(rows)
                .setTotal(ConcatenatedPageWindow.total(counts));
        result.setOffset(pagination.getOffset());
        result.setLimit(pagination.getLimit());
        return result;
    }

    @Override
    protected long doCount(Properties properties, TwinSearchEntity search, Map<String, String> namedParams, BasicSearch narrow, Set<UUID> stack) throws ServiceException {
        return ConcatenatedPageWindow.total(counts(children(properties), namedParams, narrow, stack));
    }

    private List<TwinSearchEntity> children(Properties properties) throws ServiceException {
        List<UUID> ids = childTwinSearchIds.extract(properties);
        if (ids.isEmpty())
            throw new ServiceException(ErrorCodeTwins.TWIN_SEARCH_CONFIG_INCORRECT, "concatenated search has no child searches");
        return twinSearchService.loadSearchesInOrder(ids);
    }

    private long[] counts(List<TwinSearchEntity> children, Map<String, String> namedParams, BasicSearch narrow, Set<UUID> stack) throws ServiceException {
        long[] counts = new long[children.size()];
        for (int i = 0; i < children.size(); i++)
            counts[i] = twinSearchService.countSearchExecutor(children.get(i), namedParams, narrow, stack);
        return counts;
    }
}
