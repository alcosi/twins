package org.twins.core.service.permission;

import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;
import org.twins.core.service.permission.PermissionService.PermissionDetectKey;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Component
@RequestScope
public class PermissionCheckRequestCache {
    private final Map<Key, Boolean> cache = new HashMap<>();

    public Boolean get(Key key) {
        return cache.get(key);
    }

    public void put(Key key, boolean value) {
        cache.put(key, value);
    }

    public record Key(UUID userId, UUID userGroupsFootprint, PermissionDetectKey permissionDetectKey, UUID permissionId) {
    }
}
