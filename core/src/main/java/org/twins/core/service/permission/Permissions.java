package org.twins.core.service.permission;

import lombok.Getter;

import java.util.UUID;

@Getter
public enum Permissions {
    DENY_ALL(UUID.fromString("00000000-0000-0000-0004-000000000001"), UUID.fromString("00000000-0000-0000-0005-000000000001")),
    TWIN_CLASS_MANAGE(UUID.fromString("00000000-0000-0000-0004-000000000002"), UUID.fromString("00000000-0000-0000-0005-000000000001")),
    TRANSITION_MANAGE(UUID.fromString("00000000-0000-0000-0004-000000000003"), UUID.fromString("00000000-0000-0000-0005-000000000001")),
    LINK_MANAGE(UUID.fromString("00000000-0000-0000-0004-000000000004"), UUID.fromString("00000000-0000-0000-0005-000000000001")),
    DOMAIN_TWINS_VIEW_ALL(UUID.fromString("00000000-0000-0000-0004-000000000005"), UUID.fromString("00000000-0000-0000-0005-000000000001"));

    private final UUID id;
    private final UUID permissionGroupId;

    Permissions(UUID id, UUID permissionGroupId) {
        this.id = id;
        this.permissionGroupId = permissionGroupId;
    }

}
