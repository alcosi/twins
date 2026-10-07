package org.twins.core.featurer.twin.executor;

import lombok.RequiredArgsConstructor;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.pagination.PaginationResult;
import org.cambium.common.pagination.SimplePagination;
import org.cambium.featurer.annotations.Featurer;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.twins.core.dao.search.TwinSearchEntity;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.domain.search.BasicSearch;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.service.twin.TwinSearchService;

import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;

/**
 * Default executor. Builds one query from the saved search predicates and sorts.
 */
@Component
@RequiredArgsConstructor
@Featurer(id = FeaturerTwins.ID_5701,
        name = "Search by predicates",
        description = "Executes a saved search from its predicates and sorts")
public class TwinSearchExecutorByPredicates extends TwinSearchExecutor {

    @Lazy
    private final TwinSearchService twinSearchService;

    @Override
    protected PaginationResult<TwinEntity> doExecute(Properties properties, TwinSearchEntity search, Map<String, String> namedParams, BasicSearch narrow, SimplePagination pagination, Set<UUID> stack) throws ServiceException {
        BasicSearch basicSearch = twinSearchService.toBasicSearch(search, namedParams, narrow);
        return twinSearchService.findTwinsUnaligned(basicSearch, pagination);
    }

    @Override
    protected long doCount(Properties properties, TwinSearchEntity search, Map<String, String> namedParams, BasicSearch narrow, Set<UUID> stack) throws ServiceException {
        return twinSearchService.count(twinSearchService.toBasicSearch(search, namedParams, narrow));
    }
}
