package org.twins.core.unit.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.twins.core.domain.ApiUser;
import org.twins.core.domain.apiuser.AdminAreaResolverHeaders;
import org.twins.core.service.auth.ApiUserResolverService;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

/**
 * {@code ApiUser#isAdminArea()}: lazy resolution through the header-based default resolver,
 * cached per request; an explicitly set resolver wins over the default one.
 */
@ExtendWith(MockitoExtension.class)
class ApiUserAdminAreaTest {

    @Mock
    ApiUserResolverService apiUserResolverService;
    @Mock
    AdminAreaResolverHeaders adminAreaResolverHeaders;

    @Test
    void defaultHeaderResolver_usedAndCached() {
        when(apiUserResolverService.getAdminAreaResolverHeaders()).thenReturn(adminAreaResolverHeaders);
        when(adminAreaResolverHeaders.resolveAdminArea()).thenReturn(true);
        ApiUser apiUser = new ApiUser(apiUserResolverService);
        assertTrue(apiUser.isAdminArea());
        assertTrue(apiUser.isAdminArea());
        verify(adminAreaResolverHeaders, times(1)).resolveAdminArea();
    }

    @Test
    void explicitResolver_winsOverDefault() {
        ApiUser apiUser = new ApiUser(apiUserResolverService)
                .setAdminAreaResolver(() -> true);
        assertTrue(apiUser.isAdminArea());
    }

    @Test
    void explicitResolver_false() {
        ApiUser apiUser = new ApiUser(apiUserResolverService)
                .setAdminAreaResolver(() -> false);
        assertFalse(apiUser.isAdminArea());
    }
}
