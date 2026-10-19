package org.twins.core.unit.service.permission;

import org.junit.jupiter.api.Test;
import org.twins.core.enums.consts.SystemIds;
import org.twins.core.service.permission.PermissionService;
import org.twins.core.service.permission.Permissions;

import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Static short-circuit of {@code PermissionService#systemUserHasPermission}: system identities
 * (schedulers) carry hardcoded permissions valid in every domain — no DB grants or group membership.
 */
class PermissionServiceSystemUserTest {

    @Test
    void notificationScheduler_hasDomainTwinsViewAll() {
        assertTrue(PermissionService.systemUserHasPermission(
                SystemIds.User.NOTIFICATION_SCHEDULER,
                false,
                Set.of(Permissions.DOMAIN_TWINS_VIEW_ALL.getId())));
    }

    @Test
    void notificationScheduler_doesNotHaveUnlistedPermissions() {
        assertFalse(PermissionService.systemUserHasPermission(
                SystemIds.User.NOTIFICATION_SCHEDULER,
                false,
                Set.of(Permissions.DOMAIN_VIEW.getId())));
    }

    @Test
    void notificationScheduler_anyOf_matchedByOne() {
        assertTrue(PermissionService.systemUserHasPermission(
                SystemIds.User.NOTIFICATION_SCHEDULER,
                true,
                Set.of(Permissions.DOMAIN_VIEW.getId(), Permissions.DOMAIN_TWINS_VIEW_ALL.getId())));
    }

    @Test
    void regularUser_noShortCut() {
        assertFalse(PermissionService.systemUserHasPermission(
                UUID.randomUUID(),
                false,
                Set.of(Permissions.DOMAIN_TWINS_VIEW_ALL.getId())));
    }
}
