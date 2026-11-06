package org.twins.core.unit.service.resource;

import org.cambium.common.exception.ServiceException;
import org.cambium.common.pagination.SimplePagination;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.util.ReflectionTestUtils;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.dao.resource.ResourceEntity;
import org.twins.core.dao.resource.ResourceRepository;
import org.twins.core.domain.ApiUser;
import org.twins.core.domain.search.ResourceSearch;
import org.twins.core.enums.SortDirection;
import org.twins.core.enums.sort.ResourceSortField;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.resource.ResourceSearchService;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ResourceSearchServiceTest extends BaseUnitTest {

    @Mock
    private ResourceRepository resourceRepository; // doubles as the JpaSpecificationExecutor

    private ResourceSearchService service;

    @BeforeEach
    void setUp() throws ServiceException {
        service = new ResourceSearchService(resourceRepository);
        AuthService authService = mock(AuthService.class);
        ApiUser apiUser = mock(ApiUser.class);
        when(apiUser.getLocale()).thenReturn(Locale.ENGLISH);
        when(apiUser.getDomainId()).thenReturn(UUID.randomUUID());
        when(authService.getApiUser()).thenReturn(apiUser);
        ReflectionTestUtils.setField(service, "authService", authService);
        lenient().when(resourceRepository.findAll(Mockito.<Specification<ResourceEntity>>any(), any(Pageable.class))).thenReturn(new PageImpl<>(List.of(new ResourceEntity())));
    }

    private static SimplePagination pagination() {
        return new SimplePagination().setLimit(10).setOffset(0);
    }

    @Test
    void search_happyPath_returnsPage() throws Exception {
        var result = service.search(new ResourceSearch(), pagination(), ResourceSortField.createdAt, SortDirection.DESC);
        assertEquals(1, result.getList().size());
    }

    @Test
    void search_defaultSortField_isCreatedAtAsc() throws Exception {
        ResourceSearchService spyService = Mockito.spy(service);
        spyService.search(new ResourceSearch(), pagination(), null, null);
        // sortField omitted -> default createdAt; sortDirection omitted -> ASC (base class contract)
        Mockito.verify(spyService).createSortSpecification(ResourceSortField.createdAt, SortDirection.ASC, Locale.ENGLISH);
    }

    @Test
    void search_descDirection_passedToSortSpecification() throws Exception {
        ResourceSearchService spyService = Mockito.spy(service);
        spyService.search(new ResourceSearch(), pagination(), ResourceSortField.createdAt, SortDirection.DESC);
        Mockito.verify(spyService).createSortSpecification(ResourceSortField.createdAt, SortDirection.DESC, Locale.ENGLISH);
    }
}
