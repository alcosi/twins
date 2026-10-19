package org.twins.core.unit.service;

import org.cambium.common.exception.ServiceException;
import org.cambium.common.pagination.SimplePagination;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.test.util.ReflectionTestUtils;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.domain.ApiUser;
import org.twins.core.domain.search.EntitySearch;
import org.twins.core.enums.SortDirection;
import org.twins.core.service.EntitySearchService;
import org.twins.core.service.auth.AuthService;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class EntitySearchServiceDefaultSortTest extends BaseUnitTest {

    enum TestSortField {alpha, beta}

    static class TestEntity {
    }

    static class TestSearch extends EntitySearch<TestEntity> {
    }

    /**
     * Records createSortSpecification invocations instead of building real sort specs;
     * the default sort field is controlled via {@link #overriddenDefault}.
     */
    static class RecordingSearchService extends EntitySearchService<TestSearch, TestEntity, TestSortField, Void> {
        final JpaSpecificationExecutor<TestEntity> executor;
        TestSortField overriddenDefault = null;
        TestSortField lastSortField;
        SortDirection lastSortDirection;
        int sortSpecInvocations = 0;

        RecordingSearchService(JpaSpecificationExecutor<TestEntity> executor) {
            this.executor = executor;
        }

        @Override
        protected TestSortField defaultSortField() {
            return overriddenDefault;
        }

        @Override
        public JpaSpecificationExecutor<TestEntity> jpaSpecificationExecutor() {
            return executor;
        }

        @Override
        public TestSearch emptySearch() {
            return new TestSearch();
        }

        @Override
        protected Class<TestEntity> entityClass() {
            return TestEntity.class;
        }

        @Override
        public Specification<TestEntity> createFilterSpecification(TestSearch search, UUID domainId, Locale locale) {
            return null;
        }

        @Override
        public Specification<TestEntity> createSortSpecification(TestSortField sortField, SortDirection sortDirection, Locale locale) {
            sortSpecInvocations++;
            lastSortField = sortField;
            lastSortDirection = sortDirection;
            return null;
        }

        @Override
        public String convertToEntityField(Void groupField) {
            return null;
        }

        @Override
        public void mapGroupedField(TestEntity entity, Void field, Object o) {
        }

        @Override
        protected TestEntity newEntity() {
            return new TestEntity();
        }
    }

    RecordingSearchService service;

    private static SimplePagination pagination() {
        return new SimplePagination().setLimit(10).setOffset(0);
    }

    @BeforeEach
    void setUp() throws ServiceException {
        AuthService authService = mock(AuthService.class);
        ApiUser apiUser = mock(ApiUser.class);
        when(apiUser.getLocale()).thenReturn(Locale.ENGLISH);
        when(authService.getApiUser()).thenReturn(apiUser);
        JpaSpecificationExecutor<TestEntity> executor = mock(JpaSpecificationExecutor.class);
        when(executor.findAll(Mockito.<Specification<TestEntity>>any(), any(Pageable.class))).thenReturn(new PageImpl<>(List.of()));

        service = new RecordingSearchService(executor);
        ReflectionTestUtils.setField(service, "authService", authService);
    }

    @Nested
    class NoDefaultOverride {

        @Test
        void noSortField_noOrderBy() throws ServiceException {
            service.search(new TestSearch(), pagination(), null, null);

            // legacy behavior preserved: no default -> createSortSpecification never invoked -> no ORDER BY
            assertEquals(0, service.sortSpecInvocations);
        }
    }

    @Nested
    class DefaultOverride {

        @Test
        void noSortField_defaultAppliedWithAscDirection() throws ServiceException {
            service.overriddenDefault = TestSortField.beta;

            service.search(new TestSearch(), pagination(), null, null);

            assertEquals(1, service.sortSpecInvocations);
            assertEquals(TestSortField.beta, service.lastSortField);
            assertEquals(SortDirection.ASC, service.lastSortDirection);
        }

        @Test
        void explicitSortFieldWinsOverDefault() throws ServiceException {
            service.overriddenDefault = TestSortField.beta;

            service.search(new TestSearch(), pagination(), TestSortField.alpha, SortDirection.DESC);

            assertEquals(1, service.sortSpecInvocations);
            assertEquals(TestSortField.alpha, service.lastSortField);
            assertEquals(SortDirection.DESC, service.lastSortDirection);
        }

        @Test
        void explicitSortField_nullDirection_becomesAsc() throws ServiceException {
            service.search(new TestSearch(), pagination(), TestSortField.alpha, null);

            assertEquals(1, service.sortSpecInvocations);
            assertEquals(TestSortField.alpha, service.lastSortField);
            assertEquals(SortDirection.ASC, service.lastSortDirection);
        }

        @Test
        void twoArgSearch_appliesDefault() throws ServiceException {
            service.overriddenDefault = TestSortField.beta;

            service.search(new TestSearch(), pagination());

            assertEquals(1, service.sortSpecInvocations);
            assertEquals(TestSortField.beta, service.lastSortField);
            assertEquals(SortDirection.ASC, service.lastSortDirection);
        }
    }
}
