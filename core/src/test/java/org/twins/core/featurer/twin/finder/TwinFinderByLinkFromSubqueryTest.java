package org.twins.core.featurer.twin.finder;

import org.cambium.common.exception.ServiceException;
import org.cambium.common.util.UuidUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.twins.core.domain.ApiUser;
import org.twins.core.domain.search.TwinSearch;
import org.twins.core.holder.EntityRequestCacheHolder;
import org.twins.core.holder.TwinSearchRecursionGuard;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.twin.TwinSearchService;

import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TwinFinderByLinkFromSubqueryTest {

    @Mock
    private TwinSearchService twinSearchService;
    @Mock
    private AuthService authService;
    @Mock
    private ApiUser apiUser;

    private TwinFinderByLinkFromSubquery finder;

    @BeforeEach
    void setUp() {
        EntityRequestCacheHolder.clear();
        TwinSearchRecursionGuard.clear();
        finder = new TwinFinderByLinkFromSubquery();
        finder.twinSearchService = twinSearchService;
    }

    @Test
    void subqueryTwinsAsLinkSrc_addsSrcAnyOf() throws ServiceException {
        UUID searchId = UUID.randomUUID();
        UUID link = UUID.randomUUID();
        UUID sourceA = UUID.randomUUID();
        UUID sourceB = UUID.randomUUID();
        when(twinSearchService.findTwinIds(eq(searchId), any(), anyInt()))
                .thenReturn(Set.of(sourceA, sourceB));

        TwinSearch twinSearch = new TwinSearch();
        finder.concat(twinSearch, properties(searchId, link, true), Map.of());

        assertEquals(Set.of(sourceA, sourceB), twinSearch.getSrcLinksAnyOfList().get(link));
        assertNull(twinSearch.getDstLinksAnyOfList());
        assertEquals(Boolean.TRUE, twinSearch.getDistinct());
    }

    @Test
    void subqueryTwinsAsLinkDst_addsDstAnyOf() throws ServiceException {
        UUID searchId = UUID.randomUUID();
        UUID link = UUID.randomUUID();
        UUID source = UUID.randomUUID();
        when(twinSearchService.findTwinIds(eq(searchId), any(), anyInt()))
                .thenReturn(Set.of(source));

        TwinSearch twinSearch = new TwinSearch();
        finder.concat(twinSearch, properties(searchId, link, false), Map.of());

        assertEquals(Set.of(source), twinSearch.getDstLinksAnyOfList().get(link));
        assertNull(twinSearch.getSrcLinksAnyOfList());
    }

    @Test
    void emptySubquery_matchesNothing() throws ServiceException {
        UUID searchId = UUID.randomUUID();
        UUID link = UUID.randomUUID();
        when(twinSearchService.findTwinIds(eq(searchId), any(), anyInt()))
                .thenReturn(Set.of());

        TwinSearch twinSearch = new TwinSearch();
        finder.concat(twinSearch, properties(searchId, link, true), Map.of());

        assertTrue(twinSearch.getTwinIdList().contains(UuidUtils.NULLIFY_MARKER));
        assertNull(twinSearch.getSrcLinksAnyOfList());
    }

    @Test
    void twoFinders_shareSubqueryIdsAndKeepTheirOwnLink() throws ServiceException {
        UUID searchId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID domainId = UUID.randomUUID();
        UUID firstLink = UUID.randomUUID();
        UUID secondLink = UUID.randomUUID();
        UUID portionA = UUID.randomUUID();
        UUID portionB = UUID.randomUUID();
        when(authService.getApiUser()).thenReturn(apiUser);
        when(apiUser.getUserId()).thenReturn(userId);
        when(apiUser.getDomainId()).thenReturn(domainId);
        when(twinSearchService.findTwinIds(eq(searchId), any(), anyInt()))
                .thenReturn(Set.of(portionA, portionB));
        finder.authService = authService;

        TwinFinderByLinkFromSubquery second = new TwinFinderByLinkFromSubquery();
        second.twinSearchService = twinSearchService;
        second.authService = authService;

        TwinSearch firstSearch = new TwinSearch();
        TwinSearch secondSearch = new TwinSearch();
        finder.concat(firstSearch, properties(searchId, firstLink, true), Map.of());
        second.concat(secondSearch, properties(searchId, secondLink, true), Map.of());

        assertEquals(Set.of(portionA, portionB), firstSearch.getSrcLinksAnyOfList().get(firstLink));
        assertEquals(Set.of(portionA, portionB), secondSearch.getSrcLinksAnyOfList().get(secondLink));
        verify(twinSearchService, times(1)).findTwinIds(eq(searchId), any(), anyInt());
    }

    @Test
    void circularSubquery_failsFast() throws ServiceException {
        UUID searchId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID domainId = UUID.randomUUID();
        UUID link = UUID.randomUUID();
        when(authService.getApiUser()).thenReturn(apiUser);
        when(apiUser.getUserId()).thenReturn(userId);
        when(apiUser.getDomainId()).thenReturn(domainId);
        finder.authService = authService;
        // the subquery re-enters the same finder with the same search id while it is being resolved
        when(twinSearchService.findTwinIds(eq(searchId), any(), anyInt()))
                .thenAnswer(invocation -> {
                    finder.concat(new TwinSearch(), properties(searchId, link, true), Map.of());
                    return Set.of();
                });

        ServiceException e = assertThrows(ServiceException.class,
                () -> finder.concat(new TwinSearch(), properties(searchId, link, true), Map.of()));

        // the shared saved-search recursion guard names the cycle
        assertTrue(e.getMessage().contains("circular saved search"), "message was: " + e.getMessage());
    }


    private static Properties properties(UUID searchId, UUID link, boolean sourceIsSrc) {
        Properties properties = new Properties();
        properties.setProperty("subQueryTwinSearchId", searchId.toString());
        properties.setProperty("linkId", link.toString());
        properties.setProperty("sourceIsSrc", Boolean.toString(sourceIsSrc));
        properties.setProperty("exclude", "false");
        properties.setProperty("anyOfList", "true");
        properties.setProperty("distinct", "true");
        properties.setProperty("subqueryTwinsLimit", "1000");
        return properties;
    }
}
