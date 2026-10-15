package org.twins.core.featurer.twin.executor;

import org.cambium.common.exception.ServiceException;
import org.cambium.common.pagination.PaginationResult;
import org.cambium.common.pagination.SimplePagination;
import org.cambium.featurer.annotations.FeaturerType;
import org.twins.core.dao.search.TwinSearchEntity;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.domain.search.BasicSearch;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.holder.TwinSearchRecursionGuard;

import java.util.Map;
import java.util.Properties;

/**
 * Chooses how one saved search is executed. The default implementation builds the query from
 * predicates and sorts. Another implementation may concatenate several saved searches.
 * <p>
 * The recursion guard is fully owned by this class: every public {@link #execute}/{@link #count}
 * call pushes the search id into {@link TwinSearchRecursionGuard}, so a saved search that reaches
 * itself — through child searches or through subquery finders — fails instead of looping.
 * Implementations never see the guard and cannot break it.
 */
@FeaturerType(id = FeaturerTwins.TYPE_57,
        name = "TwinSearchExecutor",
        description = "Executes one saved twin search")
public abstract class TwinSearchExecutor extends FeaturerTwins {

    public final PaginationResult<TwinEntity> execute(TwinSearchEntity search, Map<String, String> namedParams, BasicSearch narrow, SimplePagination pagination) throws ServiceException {
        TwinSearchRecursionGuard.push(search.getId());
        try {
            return doExecute(properties(search), search, namedParams, narrow, pagination);
        } finally {
            TwinSearchRecursionGuard.pop(search.getId());
        }
    }

    public final long count(TwinSearchEntity search, Map<String, String> namedParams, BasicSearch narrow) throws ServiceException {
        TwinSearchRecursionGuard.push(search.getId());
        try {
            return doCount(properties(search), search, namedParams, narrow);
        } finally {
            TwinSearchRecursionGuard.pop(search.getId());
        }
    }

    /**
     * Runs a child saved search by its own executor; the guard of the public entry applies.
     * Child pagination is used as given — the aligned-offset check is the top-level entry's duty.
     */
    protected final PaginationResult<TwinEntity> executeChild(TwinSearchEntity child, Map<String, String> namedParams, BasicSearch narrow, SimplePagination pagination) throws ServiceException {
        return executor(child).execute(child, namedParams, narrow, pagination);
    }

    /**
     * Counts a child saved search by its own executor; the guard of the public entry applies.
     */
    protected final long countChild(TwinSearchEntity child, Map<String, String> namedParams, BasicSearch narrow) throws ServiceException {
        return executor(child).count(child, namedParams, narrow);
    }

    private TwinSearchExecutor executor(TwinSearchEntity search) throws ServiceException {
        return featurerService.getFeaturer(search.getTwinSearchExecutorFeaturerId(), TwinSearchExecutor.class);
    }

    protected abstract PaginationResult<TwinEntity> doExecute(Properties properties, TwinSearchEntity search, Map<String, String> namedParams, BasicSearch narrow, SimplePagination pagination) throws ServiceException;

    protected abstract long doCount(Properties properties, TwinSearchEntity search, Map<String, String> namedParams, BasicSearch narrow) throws ServiceException;

    private Properties properties(TwinSearchEntity search) throws ServiceException {
        return featurerService.extractProperties(this, search.getTwinSearchExecutorParams());
    }
}
