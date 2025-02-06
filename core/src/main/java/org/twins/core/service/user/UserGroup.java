package org.twins.core.service.user;

import lombok.RequiredArgsConstructor;

import java.util.UUID;

@RequiredArgsConstructor
public enum UserGroup {
    DOMAIN_ADMIN(UUID.fromString("00000000-0000-0000-0006-000000000001"));
    public final UUID uuid;
}
