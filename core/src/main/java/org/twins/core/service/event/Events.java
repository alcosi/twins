package org.twins.core.service.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@Getter
@RequiredArgsConstructor
public enum Events {
    SIGNUP_EMAIL_VERIFICATION_CODE(UUID.fromString("00000000-0000-0002-0001-000000000001")),
    SIGNUP_EMAIL_VERIFICATION_LINK(UUID.fromString("00000000-0000-0002-0001-000000000002")),
    ;

    private final UUID id;
}
