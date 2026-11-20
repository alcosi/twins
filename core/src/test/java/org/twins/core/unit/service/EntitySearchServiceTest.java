package org.twins.core.unit.service;

import org.cambium.common.exception.ServiceException;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.twins.core.domain.search.EntitySearch;
import org.twins.core.enums.SortDirection;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.service.EntitySearchService;

import java.util.*;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

/**
 * The findAllUnpaged contract shared by every entity search: the search is converted to a filter
 * inside, then a single fetch reads at most limit + 1 rows — one row over the limit is the
 * overflow marker, no separate count query.
 */
class EntitySearchServiceTest {

    private static final class TestSearch extends EntitySearch<Object> {
    }

    private final JpaSpecificationExecutor<Object> executor = mock(JpaSpecificationExecutor.class);
    private final Specification<Object> filter = (root, query, cb) -> null;
    private final TestSearch emptySearch = new TestSearch();
    private TestSearch lastSearch;

    private final EntitySearchService<TestSearch, Object, Object, Object> service = new EntitySearchService<>() {
        @Override
        public JpaSpecificationExecutor<Object> jpaSpecificationExecutor() {
            return executor;
        }

        @Override
        public TestSearch emptySearch() {
            return emptySearch;
        }

        @Override
        protected Class<Object> entityClass() {
            return Object.class;
        }

        @Override
        public Specification<Object> createFilterSpecification(TestSearch search, UUID domainId, Locale locale) {
            return null;
        }

        @Override
        protected Specification<Object> createFilterSpecification(TestSearch search) {
            // no Spring here: the base implementation resolves the api user, this one just records
            lastSearch = search;
            return filter;
        }

        @Override
        public Specification<Object> createSortSpecification(Object sortField, SortDirection sortDirection, Locale locale) {
            return null;
        }

        @Override
        public String convertToEntityField(Object groupFields) {
            return null;
        }

        @Override
        protected Object defaultSortField() {
            return null;
        }

        @Override
        public void mapGroupedField(Object entity, Object field, Object o) {
        }

        @Override
        protected Object newEntity() {
            return new Object();
        }
    };

    @Test
    void selectionAboveTheLimitIsRejected() {
        List<Object> over = new ArrayList<>(Collections.nCopies(11, "row"));
        when(executor.findBy(any(Specification.class), any(Function.class))).thenReturn(over);

        ServiceException exception = assertThrows(ServiceException.class,
                () -> service.findAllUnpaged(new TestSearch(), null, 10));
        assertEquals(ErrorCodeTwins.SEARCH_RESULT_LIMIT_EXCEEDED.getCode(), exception.getErrorCode());
    }

    @Test
    void limitBoundaryIsAccepted() throws Exception {
        List<Object> atLimit = new ArrayList<>(Collections.nCopies(10, "row"));
        when(executor.findBy(any(Specification.class), any(Function.class))).thenReturn(atLimit);

        assertEquals(atLimit, service.findAllUnpaged(new TestSearch(), null, 10));
    }

    @Test
    void fetchReadsOneRowOverTheLimitAsTheOverflowMarker() throws Exception {
        when(executor.findBy(any(Specification.class), any(Function.class))).thenReturn(List.of());

        service.findAllUnpaged(new TestSearch(), null, 10);

        ArgumentCaptor<Function> captor = ArgumentCaptor.forClass(Function.class);
        verify(executor).findBy(any(Specification.class), captor.capture());
        JpaSpecificationExecutor.SpecificationFluentQuery<Object> query =
                mock(JpaSpecificationExecutor.SpecificationFluentQuery.class);
        when(query.limit(anyInt())).thenReturn(query);
        captor.getValue().apply(query);
        verify(query).limit(11);
    }

    @Test
    void nullSearchFallsBackToEmptySearch() throws Exception {
        when(executor.findBy(any(Specification.class), any(Function.class))).thenReturn(List.of());

        service.findAllUnpaged(null, null, 10);

        assertSame(emptySearch, lastSearch);
    }

    @Test
    void nullSortFetchesByTheFilterAsIs() throws Exception {
        when(executor.findBy(any(Specification.class), any(Function.class))).thenReturn(List.of("one"));

        assertEquals(List.of("one"), service.findAllUnpaged(new TestSearch(), null, 10));
        ArgumentCaptor<Specification<Object>> captor = ArgumentCaptor.forClass(Specification.class);
        verify(executor).findBy(captor.capture(), any(Function.class));
        assertSame(filter, captor.getValue());
    }

    @Test
    void sortIsComposedIntoTheFetchSpec() throws Exception {
        Specification<Object> sort = (root, query, cb) -> null;
        when(executor.findBy(any(Specification.class), any(Function.class))).thenReturn(List.of("one"));

        assertEquals(List.of("one"), service.findAllUnpaged(new TestSearch(), sort, 10));
        ArgumentCaptor<Specification<Object>> captor = ArgumentCaptor.forClass(Specification.class);
        verify(executor).findBy(captor.capture(), any(Function.class));
        assertNotSame(filter, captor.getValue());
    }
}
