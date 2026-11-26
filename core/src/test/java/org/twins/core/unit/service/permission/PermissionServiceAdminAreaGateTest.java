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
 * choke point for the DOMAIN_TWINS_VIEW_ALL bypass: forked by thread type. In a request-serving
 * thread the bypass requires the AdminArea header plus the permission; in a background thread
 * (schedulers, async) only a system identity with the hardcoded grant bypasses.
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
    void systemUser_inBackgroundThread_bypasses() throws Exception {
        when(apiUser.getUserId()).thenReturn(SystemIds.User.NOTIFICATION_SCHEDULER);
        assertTrue(permissionService.currentUserCanViewAllTwinsInDomain());
    }

    @Test
    void systemUser_inHttpThread_needsHeaderLikeEveryoneElse() throws Exception {
        // the system-grant carve-out applies only to background threads; in a request-serving
        // thread even a system identity bypasses only from the admin area
        when(apiUser.getUserId()).thenReturn(SystemIds.User.NOTIFICATION_SCHEDULER);
        when(apiUser.isRemoteRequest()).thenReturn(true);
        when(apiUser.isAdminArea()).thenReturn(true);
        assertTrue(permissionService.currentUserCanViewAllTwinsInDomain());
    }

    @Test
    void regularUser_withPermissionAndHeader_bypasses() throws Exception {
        when(apiUser.getUserId()).thenReturn(UUID.randomUUID());
        when(apiUser.getUser()).thenReturn(new UserEntity().setPermissions(Set.of(Permissions.DOMAIN_TWINS_VIEW_ALL.getId())));
        when(apiUser.isRemoteRequest()).thenReturn(true);
        when(apiUser.isAdminArea()).thenReturn(true);
        assertTrue(permissionService.currentUserCanViewAllTwinsInDomain());
    }

    @Test
    void regularUser_inUserAreaRequest_fallsBackToNormalChecks() throws Exception {
        // suppression is silent in user-area requests, and the permission is never even loaded
        // there (short-circuit on isAdminArea)
        when(apiUser.isRemoteRequest()).thenReturn(true);
        when(apiUser.isAdminArea()).thenReturn(false);
        assertFalse(permissionService.currentUserCanViewAllTwinsInDomain());
    }

    @Test
    void regularUser_inBackgroundThread_noBypassEvenWithPermission() throws Exception {
        // background threads bypass only via the hardcoded system grant — a regular user holding
        // DOMAIN_TWINS_VIEW_ALL gets nothing here (pre-TWINS-950 they did)
        when(apiUser.getUserId()).thenReturn(UUID.randomUUID());
        when(apiUser.isRemoteRequest()).thenReturn(false);
        assertFalse(permissionService.currentUserCanViewAllTwinsInDomain());
    }

    @Test
    void regularUser_withHeaderWithoutPermission_fallsBackToNormalChecks() throws Exception {
        when(apiUser.getUserId()).thenReturn(UUID.randomUUID());
        when(apiUser.getUser()).thenReturn(new UserEntity().setPermissions(Collections.emptySet()));
        when(apiUser.isRemoteRequest()).thenReturn(true);
        when(apiUser.isAdminArea()).thenReturn(true);
        assertFalse(permissionService.currentUserCanViewAllTwinsInDomain());
    }

    @Test
    void anonymousUser_deniedEvenWithHeader() throws Exception {
        // pins the fail-closed check order: user-specified first, thread-type fork after
        when(apiUser.isUserSpecified()).thenReturn(false);
        assertFalse(permissionService.currentUserCanViewAllTwinsInDomain());
    }
}
