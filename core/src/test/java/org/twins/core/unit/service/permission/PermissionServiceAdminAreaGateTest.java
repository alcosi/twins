package org.twins.core.unit.service.permission;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.twins.core.dao.user.UserEntity;
import org.twins.core.domain.ApiUser;
import org.twins.core.enums.consts.SystemIds;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.permission.PermissionService;
import org.twins.core.service.permission.Permissions;

import java.util.Collections;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

/**
 * Decision matrix of {@code PermissionService#currentUserCanViewAllTwinsInDomain()} — the single
 * choke point for the DOMAIN_TWINS_VIEW_ALL bypass, gated by the AdminArea header
 * ({@code ApiUser#isAdminArea()}).
 */
@ExtendWith(MockitoExtension.class)
class PermissionServiceAdminAreaGateTest {

    @Mock
    AuthService authService;
    @Mock
    ApiUser apiUser;
    @InjectMocks
    PermissionService permissionService;

    @BeforeEach
    void setUp() throws Exception {
        when(authService.getApiUser()).thenReturn(apiUser);
        when(apiUser.isUserSpecified()).thenReturn(true);
    }

    @Test
    void systemUser_bypassesWithoutHeader() throws Exception {
        when(apiUser.getUserId()).thenReturn(SystemIds.User.NOTIFICATION_SCHEDULER);
        assertTrue(permissionService.currentUserCanViewAllTwinsInDomain());
    }

    @Test
    void regularUser_withPermissionAndHeader_bypasses() throws Exception {
        when(apiUser.getUserId()).thenReturn(UUID.randomUUID());
        when(apiUser.getUser()).thenReturn(new UserEntity().setPermissions(Set.of(Permissions.DOMAIN_TWINS_VIEW_ALL.getId())));
        when(apiUser.isAdminArea()).thenReturn(true);
        assertTrue(permissionService.currentUserCanViewAllTwinsInDomain());
    }

    @Test
    void regularUser_withPermissionWithoutHeader_fallsBackToNormalChecks() throws Exception {
        when(apiUser.getUserId()).thenReturn(UUID.randomUUID());
        when(apiUser.isAdminArea()).thenReturn(false);
        assertFalse(permissionService.currentUserCanViewAllTwinsInDomain());
    }

    @Test
    void regularUser_withHeaderWithoutPermission_fallsBackToNormalChecks() throws Exception {
        when(apiUser.getUserId()).thenReturn(UUID.randomUUID());
        when(apiUser.getUser()).thenReturn(new UserEntity().setPermissions(Collections.emptySet()));
        when(apiUser.isAdminArea()).thenReturn(true);
        assertFalse(permissionService.currentUserCanViewAllTwinsInDomain());
    }
}
