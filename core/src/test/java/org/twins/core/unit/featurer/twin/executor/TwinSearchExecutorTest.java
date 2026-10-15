package org.twins.core.unit.featurer.twin.executor;

import org.cambium.common.exception.ServiceException;
import org.cambium.common.pagination.PaginationResult;
import org.cambium.common.pagination.SimplePagination;
import org.cambium.featurer.Featurer;
import org.cambium.featurer.FeaturerService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.twins.core.dao.search.TwinSearchEntity;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.domain.search.BasicSearch;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.twin.executor.TwinSearchExecutor;
import org.twins.core.holder.TwinSearchRecursionGuard;

import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Recursion-guard contract of the public executor entries. The guard is invisible to
 * implementations — these tests re-enter a search from inside {@code doExecute} and expect a
 * clear config error instead of infinite recursion.
 */
@ExtendWith(MockitoExtension.class)
class TwinSearchExecutorTest {

    @Mock
    private FeaturerService featurerService;

    @BeforeEach
    void setUp() {
        TwinSearchRecursionGuard.clear();
    }

    @AfterEach
    void cleanup() {
        TwinSearchRecursionGuard.clear();
    }

    @Test
    void execute_reEntersTheSameSearch_failsInsteadOfRecursing() throws ServiceException {
        TwinSearchEntity search = new TwinSearchEntity().setId(UUID.randomUUID());
        TwinSearchExecutor executor = reentrantThroughExecute(search);

        ServiceException e = assertThrows(ServiceException.class,
                () -> executor.execute(search, Map.of(), null, pagination()));

        assertEquals(ErrorCodeTwins.TWIN_SEARCH_CONFIG_INCORRECT.getCode(), e.getErrorCode());
        assertTrue(e.getMessage().contains("circular saved search"), "message was: " + e.getMessage());
    }

    @Test
    void executeChild_childIsTheSearchItself_failsInsteadOfRecursing() throws ServiceException {
        TwinSearchEntity search = new TwinSearchEntity()
                .setId(UUID.randomUUID())
                .setTwinSearchExecutorFeaturerId(5702); // resolves to the same executor
        TwinSearchExecutor executor = reentrantThroughExecuteChild(search);

        ServiceException e = assertThrows(ServiceException.class,
                () -> executor.execute(search, Map.of(), null, pagination()));

        assertEquals(ErrorCodeTwins.TWIN_SEARCH_CONFIG_INCORRECT.getCode(), e.getErrorCode());
        assertTrue(e.getMessage().contains("circular saved search"), "message was: " + e.getMessage());
    }

    @Test
    void execute_sequentialRunsOfTheSameSearch_areLegal() throws ServiceException {
        TwinSearchEntity search = new TwinSearchEntity().setId(UUID.randomUUID());
        CountingExecutor executor = (CountingExecutor) init(new CountingExecutor());

        executor.execute(search, Map.of(), null, pagination());
        executor.execute(search, Map.of(), null, pagination()); // pop worked — no cycle detected

        assertEquals(2, executor.runs);
    }

    // doExecute re-enters the same search through the public execute entry
    private TwinSearchExecutor reentrantThroughExecute(TwinSearchEntity search) throws ServiceException {
        TwinSearchExecutor executor = new TwinSearchExecutor() {
            @Override
            protected PaginationResult<TwinEntity> doExecute(Properties properties, TwinSearchEntity s, Map<String, String> namedParams, BasicSearch narrow, SimplePagination pagination) throws ServiceException {
                return execute(search, Map.of(), null, pagination());
            }

            @Override
            protected long doCount(Properties properties, TwinSearchEntity s, Map<String, String> namedParams, BasicSearch narrow) {
                return 0;
            }
        };
        return init(executor);
    }

    // doExecute runs the search as its own child — the old "concat" cycle shape
    private TwinSearchExecutor reentrantThroughExecuteChild(TwinSearchEntity search) throws ServiceException {
        TwinSearchExecutor executor = new TwinSearchExecutor() {
            @Override
            protected PaginationResult<TwinEntity> doExecute(Properties properties, TwinSearchEntity s, Map<String, String> namedParams, BasicSearch narrow, SimplePagination pagination) throws ServiceException {
                return executeChild(search, Map.of(), null, pagination());
            }

            @Override
            protected long doCount(Properties properties, TwinSearchEntity s, Map<String, String> namedParams, BasicSearch narrow) {
                return 0;
            }
        };
        when(featurerService.getFeaturer(any(Integer.class), any())).thenReturn(executor); // the child resolves back to this executor
        return init(executor);
    }

    // completes a normal run — proves the guard does not trip on sequential (non-nested) calls
    private class CountingExecutor extends TwinSearchExecutor {
        int runs = 0;

        @Override
        protected PaginationResult<TwinEntity> doExecute(Properties properties, TwinSearchEntity s, Map<String, String> namedParams, BasicSearch narrow, SimplePagination pagination) {
            runs++;
            return new PaginationResult<TwinEntity>().setList(List.of()).setTotal(0);
        }

        @Override
        protected long doCount(Properties properties, TwinSearchEntity s, Map<String, String> namedParams, BasicSearch narrow) {
            return 0;
        }
    }

    private TwinSearchExecutor init(TwinSearchExecutor executor) throws ServiceException {
        executor.featurerService = featurerService;
        when(featurerService.extractProperties(any(Featurer.class), any())).thenReturn(new Properties());
        return executor;
    }

    private static SimplePagination pagination() {
        return new SimplePagination().setOffset(0).setLimit(10);
    }
}
