package org.twins.core.featurer.twin.executor;

import org.cambium.common.exception.ServiceException;
import org.cambium.common.pagination.PaginationResult;
import org.cambium.common.pagination.SimplePagination;
import org.cambium.featurer.annotations.FeaturerType;
import org.twins.core.dao.search.TwinSearchEntity;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.domain.search.BasicSearch;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.FeaturerTwins;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;

/**
 * Chooses how one saved search is executed. The default implementation builds the query from
 * predicates and sorts. Another implementation may concatenate several saved searches.
 * <p>
 * The recursion guard is fully owned by this class: {@link #execute(TwinSearchEntity, Map, BasicSearch, SimplePagination)}
 * and {@link #count(TwinSearchEntity, Map, BasicSearch)} start a fresh guard, and their stack-carrying
 * overloads are package-private, so the guard set can only be created here and threaded by the
 * implementations themselves (via {@link #executeChild} / {@link #countChild}) — a saved search that
 * reaches itself through child searches fails instead of looping.
 */
@FeaturerType(id = FeaturerTwins.TYPE_57,
        name = "TwinSearchExecutor",
        description = "Executes one saved twin search")
public abstract class TwinSearchExecutor extends FeaturerTwins {

    public final PaginationResult<TwinEntity> execute(TwinSearchEntity search, Map<String, String> namedParams, BasicSearch narrow, SimplePagination pagination) throws ServiceException {
        return execute(search, namedParams, narrow, pagination, new LinkedHashSet<>());
    }

    final PaginationResult<TwinEntity> execute(TwinSearchEntity search, Map<String, String> namedParams, BasicSearch narrow, SimplePagination pagination, Set<UUID> stack) throws ServiceException {
        boolean pushed = push(search, stack);
        try {
            return doExecute(properties(search), search, namedParams, narrow, pagination, stack);
        } finally {
            pop(search, stack, pushed);
        }
    }

    public final long count(TwinSearchEntity search, Map<String, String> namedParams, BasicSearch narrow) throws ServiceException {
        return count(search, namedParams, narrow, new LinkedHashSet<>());
    }

    final long count(TwinSearchEntity search, Map<String, String> namedParams, BasicSearch narrow, Set<UUID> stack) throws ServiceException {
        boolean pushed = push(search, stack);
        try {
            return doCount(properties(search), search, namedParams, narrow, stack);
        } finally {
            pop(search, stack, pushed);
        }
    }

    /**
     * Runs a child saved search by its own executor within the same recursion guard.
     * Child pagination is used as given — the aligned-offset check is the top-level entry's duty.
     */
    protected final PaginationResult<TwinEntity> executeChild(TwinSearchEntity child, Map<String, String> namedParams, BasicSearch narrow, SimplePagination pagination, Set<UUID> stack) throws ServiceException {
        return executor(child).execute(child, namedParams, narrow, pagination, stack);
    }

    /**
     * Counts a child saved search by its own executor within the same recursion guard.
     */
    protected final long countChild(TwinSearchEntity child, Map<String, String> namedParams, BasicSearch narrow, Set<UUID> stack) throws ServiceException {
        return executor(child).count(child, namedParams, narrow, stack);
    }

    private TwinSearchExecutor executor(TwinSearchEntity search) throws ServiceException {
        return featurerService.getFeaturer(search.getTwinSearchExecutorFeaturerId(), TwinSearchExecutor.class);
    }

    protected abstract PaginationResult<TwinEntity> doExecute(Properties properties, TwinSearchEntity search, Map<String, String> namedParams, BasicSearch narrow, SimplePagination pagination, Set<UUID> stack) throws ServiceException;

    protected abstract long doCount(Properties properties, TwinSearchEntity search, Map<String, String> namedParams, BasicSearch narrow, Set<UUID> stack) throws ServiceException;

    private Properties properties(TwinSearchEntity search) throws ServiceException {
        return featurerService.extractProperties(this, search.getTwinSearchExecutorParams());
    }

    private boolean push(TwinSearchEntity search, Set<UUID> stack) throws ServiceException {
        UUID id = search.getId();
        if (id == null)
            return false;
        if (!stack.add(id))
            throw new ServiceException(ErrorCodeTwins.TWIN_SEARCH_CONFIG_INCORRECT, "saved search recursion: " + id);
        return true;
    }

    private void pop(TwinSearchEntity search, Set<UUID> stack, boolean pushed) {
        if (pushed)
            stack.remove(search.getId());
    }
}
