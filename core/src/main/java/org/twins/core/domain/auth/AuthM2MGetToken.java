package org.twins.core.domain.auth;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.UUID;

@Data
@Accessors(chain = true)
public class AuthM2MGetToken {
    private String clientId;
    private String clientSecret;
    private UUID publicKeyId;
}
